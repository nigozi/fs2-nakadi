package fs2.nakadi.interpreters

import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{Async, ContextShift}
import cats.instances.option._
import cats.syntax.functor._
import cats.syntax.traverse._
import fs2.nakadi.model.{NakadiConfig, Token}
import org.http4s
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, Request}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

trait HttpClient {
  private def authHeader(token: Token): Header =
    Authorization(http4s.Credentials.Token(CaseInsensitiveString("Bearer"), token.value))

  def defaultClient[F[_]: Async](implicit cs: ContextShift[F]): Client[F] = {
    val blockingEC: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

    JavaNetClientBuilder[F](blockingEC).create
  }

  def addBaseHeaders[F[_]: Async](req: Request[F],
                                  config: NakadiConfig[F],
                                  headers: List[Header] = Nil): F[Request[F]] = {
    val baseHeaders = Header("X-Flow-ID", UUID.randomUUID().toString) :: headers
    val allHeaders = config.tokenProvider.traverse(_.provider.apply().map(authHeader)).map {
      case Some(h) => h :: baseHeaders
      case None    => baseHeaders
    }

    allHeaders.map(h => req.putHeaders(h: _*))
  }
}

object HttpClient extends HttpClient
