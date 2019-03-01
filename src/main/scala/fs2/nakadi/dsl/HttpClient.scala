package fs2.nakadi.dsl
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import cats.syntax.applicative._
import cats.syntax.option._
import com.typesafe.scalalogging.Logger
import fs2.nakadi.model.{FlowId, NakadiConfig, Token}
import org.http4s
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Header, Headers, Request, Response, Status}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

trait HttpClient {
  private val logger = Logger("HttpHelper")

  def defaultClient: Client[IO] = {
    implicit val cs: ContextShift[IO] = IO.contextShift(global)

    val blockingEC: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

    JavaNetClientBuilder(blockingEC).create[IO]
  }

  def toHeader(token: Token): Header =
    Authorization(http4s.Credentials.Token(CaseInsensitiveString("Bearer"), token.value))

  def baseHeaders(config: NakadiConfig)(implicit flowId: FlowId): IO[Headers] = {
    val base = List(Header("X-Flow-ID", flowId.id))

    val headers = config.tokenProvider match {
      case Some(tp) => tp.provider.apply().map(toHeader).map(_ :: base)
      case None     => base.pure[IO]
    }

    headers.map(h => Headers(h))
  }

  def fetch[A](request: Request[IO], config: NakadiConfig)(
      f: Response[IO] => IO[Nothing])(implicit flowId: FlowId, dec: EntityDecoder[IO, A]): IO[A] = {
    val httpClient = config.httpClient.getOrElse(defaultClient)

    for {
      headers <- baseHeaders(config)
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[A](request.withHeaders(headers)) {
                   case Status.Successful(l) => l.as[A]
                   case r                    => f(r)
                 }
    } yield response
  }

  def fetchOpt[A](request: Request[IO], config: NakadiConfig)(
      f: Response[IO] => IO[Nothing])(implicit flowId: FlowId, dec: EntityDecoder[IO, A]): IO[Option[A]] = {
    val httpClient = config.httpClient.getOrElse(defaultClient)

    for {
      headers <- baseHeaders(config)
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[Option[A]](request.withHeaders(headers)) {
                   case Status.NotFound(_)   => None.pure[IO]
                   case Status.Successful(l) => l.as[A].map(_.some)
                   case r                    => f(r)
                 }
    } yield response
  }
}

object HttpClient extends HttpClient
