package cn.enaium

import cn.enaium.service.TodoServe
import cn.enaium.utility.sql
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.babyfish.jimmer.jackson.ImmutableModule
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

/**
 * @author Enaium
 */
fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        modules(module {
            single<ApplicationEnvironment> { environment }
            single<KSqlClient> { sql(get()) }
            single<TodoServe> { TodoServe(get()) }
        })
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
    install(ContentNegotiation) {
        jackson {
            registerModules(ImmutableModule())
        }
    }

    val todoServe by inject<TodoServe>()

    routing {
        get("/task") {
            call.respond(todoServe.getTasks())
        }
        post("/task") {
            todoServe.saveTask(call.receive())
            call.response.status(HttpStatusCode.OK)
        }
    }
}