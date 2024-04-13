@file:OptIn(ExperimentalSerializationApi::class)

import emotion.react.css
import js.date.Date
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import org.w3c.fetch.RequestInit
import react.*
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.px
import web.dom.document
import kotlin.js.json

/**
 * @author Enaium
 */
fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(container).render(App.create())
}

@Serializable
data class Task(val id: String, var name: String, val startTime: Long, val endTime: Long?) {
    fun copy(name: String = this.name, startTime: Long = this.startTime, endTime: Long? = this.endTime) =
        Task(id, name, startTime, endTime)

    fun toInput() = TaskInput(id, name, startTime, endTime)
}

@Serializable
data class TaskInput(
    val id: String? = null,
    val name: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null
)

val coroutine = CoroutineScope(window.asCoroutineDispatcher())

suspend fun fetchTasks(): List<Task> {
    window.fetch("http://localhost:8080/task").await().let {
        if (it.status != 200.toShort()) {
            throw Exception("Failed to fetch")
        }
        return Json.decodeFromDynamic<List<Task>>(it.json().await())
    }
}

suspend fun saveTask(task: TaskInput) {
    window.fetch(
        "http://localhost:8080/task",
        RequestInit(
            method = "POST",
            body = Json.encodeToString(TaskInput.serializer(), task),
            headers = json("Content-Type" to "application/json")
        )
    ).await().let {
        if (it.status != 200.toShort()) {
            throw Exception("Failed to save")
        }
    }
}

val App = FC {
    var tasksState by useState(emptyList<Task>())
    var taskState by useState<TaskInput>()

    useEffectOnce {
        coroutine.launch {
            tasksState = fetchTasks()
        }
    }

    useEffect(listOf(taskState)) {
        taskState?.let {
            coroutine.launch {
                saveTask(it)
                window.location.reload()
            }
        }
    }

    div {
        input {
            css {
                fontSize = 24.px
            }

            onKeyUp = {
                if (it.asDynamic().key == "Enter") {
                    taskState = TaskInput(name = it.target.asDynamic().value as String)
                }
            }
        }
        div {
            css {
                marginTop = 10.px
                display = Display.flex
                flexDirection = FlexDirection.column
                gap = 10.px
            }
            tasksState.forEach {
                TaskItem {
                    key = it.id
                    task = it
                }
            }
        }
    }
}

external interface TaskItemProps : Props {
    var task: Task
}

val TaskItem = FC<TaskItemProps> { props ->
    var editState by useState(false)

    var taskState by useState<TaskInput>()

    useEffect(listOf(taskState)) {
        taskState?.let {
            coroutine.launch {
                saveTask(it)
                window.location.reload()
            }
        }
    }

    div {
        if (editState) {
            input {
                defaultValue = props.task.name
                onKeyUp = {
                    if (it.asDynamic().key == "Enter") {
                        taskState = props.task.copy(name = it.target.asDynamic().value as String).toInput()
                        editState = false
                    }

                    if (it.asDynamic().key == "Escape") {
                        editState = false
                    }
                }
            }
        } else {
            div {
                css {
                    color = if (props.task.endTime == null) Color("red") else Color("green")
                }
                div {
                    +props.task.id
                }
                div {
                    +props.task.name
                }
                div {
                    +kotlin.js.Date(props.task.startTime).toLocaleString()
                    props.task.endTime?.let {
                        +" - "
                        +kotlin.js.Date(it).toLocaleString()
                    }
                }
            }

            button {
                +"Edit"
                onClick = {
                    editState = !editState
                }
            }
            button {
                +"Finish"
                onClick = {
                    taskState = props.task.copy(endTime = Date().getTime().toLong()).toInput()
                }
            }
        }
    }
}