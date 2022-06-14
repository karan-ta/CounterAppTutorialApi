package com.example.plugins
import com.google.gson.Gson
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.serialization.gson.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class CounterDataObject(
    val id:Int,
    val counterData:Int,
)
object CounterDataTable : Table(name = "counterdata") {
    val id = integer(name = "id")
    val counterdata = integer(name = "counterdata")
}
object Dbvars {
    val host: String = "localhost"
    val dbname: String = "counter_tutorial"
    val sslmode: String = ""
    val user: String = "postgres"
    val password: String = "kodeplay2010"
}
fun Application.configureRouting() {
    val db = Database.connect("jdbc:postgresql://"+Dbvars.host+":5432/"+Dbvars.dbname+Dbvars.sslmode, driver = "org.postgresql.Driver",
        user = Dbvars.user, password = Dbvars.password)
    routing {
        get("/counterdata") {
            lateinit var counterDataDBObject:CounterDataObject
            transaction(db) {
                CounterDataTable.selectAll().limit(1).forEach{
                     counterDataDBObject = CounterDataObject(it[CounterDataTable.id],it[CounterDataTable.counterdata])
                }
                println(counterDataDBObject)
            }
            call.respondText(Gson().toJson (counterDataDBObject))
        }

        post("/createcount") {
            transaction(db) {
                CounterDataTable.insert {
                    it[this.id] = 1
                    it[this.counterdata] = 0
                }
            }
            call.respondText ("inside post "+call.parameters["count"])
            //insert data in table.
        }

        put ("/updatecount") {
            println (call.parameters["inputcount"])
            if ((call.parameters["inputcount"]) != null)
            transaction(db) {
                CounterDataTable.update({ CounterDataTable.id eq 1 }) {
                    it[counterdata] = call.parameters["inputcount"]!!.toInt()
                }
            }
            call.respondText ("inside put "+call.parameters["count"])
            //update data in table
        }
    }
}
