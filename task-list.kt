package tasklist

import kotlinx.datetime.*
import java.time.DateTimeException
import java.time.format.DateTimeFormatter

var tasks = mutableListOf<Task>()

class TasksPrinter {

    val divider = "+----+------------+-------+---+---+--------------------------------------------+"
    val header = "| N  |    Date    | Time  | P | D |                   Task                     |"
    val MAX_LENGTH = 44

    fun printTasks() {

        if (tasks.isEmpty()){
            return
        }

//        +----+------------+-------+---+---+--------------------------------------------+
//        | N  |    Date    | Time  | P | D |                   Task                     |
//        +----+------------+-------+---+---+--------------------------------------------+
//        | 1  | yyyy-MM-dd | hh:mm |   |   |                                            |
//        |    |            |       |   |   |                                            |
//        |    |            |       |   |   |                                            |
//        +----+------------+-------+---+---+--------------------------------------------+

        println(divider)
        println(header)
        println(divider)

        for (task in tasks) {
            val stringBuilderList = prepareTaskText(task)
            var isDataShowed = false

            for (stringBuilder in stringBuilderList) {
                for (string in stringBuilder.split("\n")) {
                    if(string.isEmpty()){
                        continue
                    }
                    val padEnd = string.padEnd(MAX_LENGTH, ' ')

                    if (!isDataShowed) {
                        val dateTimeArray = task.dateTime.toString().replace("T", " ").split(" ")
                        val date = dateTimeArray[0]
                        val time = dateTimeArray[1]
                        val dueTagColor = getDueTagColor(task)
                        val priorityColor = getPriorityColor(task)
                        println("| ${task.id}${task.getTaskDivider()}| $date | $time | $priorityColor | $dueTagColor |$padEnd|")
                        isDataShowed = true
                    } else {
                        println("|    |            |       |   |   |$padEnd|")
                    }
                }
            }
            println(divider)
        }
    }


    private fun getPriorityColor(task: Task): String {


        return when (task.priority) {
            "C" -> {
                "\u001B[101m \u001B[0m"
            }

            "H" -> {
                "\u001B[103m \u001B[0m"
            }

            "N" -> {
                "\u001B[102m \u001B[0m"
            }

            else -> {
                "\u001B[104m \u001B[0m"
            }
        }
    }

    private fun getDueTagColor(task: Task): String {
        return when (task.dueTag) {
            "I" -> {
                "\u001B[102m \u001B[0m"
            }

            "T" -> {
                "\u001B[103m \u001B[0m"
            }

            else -> {
                "\u001B[101m \u001B[0m"
            }
        }
    }

    private fun prepareTaskText(task: Task): MutableList<StringBuilder> {
        val newBuffer = mutableListOf<StringBuilder>()

        for (bufferTask in task.buffer) {
            val stringBuilder = StringBuilder()
            if (bufferTask.length > MAX_LENGTH) {
                var startIndex = 0
                var finishIndex = MAX_LENGTH - 1
                while (finishIndex < bufferTask.lastIndex) {
                    stringBuilder.appendLine(bufferTask.substring(startIndex, finishIndex))
                    startIndex = finishIndex
                    finishIndex = if ((finishIndex + MAX_LENGTH) > bufferTask.lastIndex) {
                        bufferTask.lastIndex
                    } else {
                        finishIndex + MAX_LENGTH
                    }
                }

            } else {
                stringBuilder.appendLine(bufferTask)
            }

            newBuffer.add(stringBuilder)
        }

        return newBuffer
    }
}

class Task {
    var priority: String = ""
    var dateTime: LocalDateTime? = null
    var buffer = mutableListOf<String>()
    var id: Int? = null
    var dueTag: String = ""

    private val CORRECT_PRIORITIES = mutableListOf("C", "H", "N", "L")

    fun setPriority(inputPriority: String): Boolean {
        val isPriorityCorrect: Boolean
        val element = inputPriority.trim().uppercase()
        isPriorityCorrect = CORRECT_PRIORITIES.contains(element)

        if (isPriorityCorrect) {
            priority = element
        }

        return isPriorityCorrect
    }

    fun setDate(inputString: String): Boolean {
        var isDateCorrect = inputString.matches(Regex("\\d\\d\\d\\d-\\d{1,2}-\\d{1,2}"))
        if (!isDateCorrect) {
            println("The input date is invalid")
        } else {
            val splitDate = inputString.split("-")
            val year = splitDate[0].toInt()
            val month = splitDate[1].toInt()
            val day = splitDate[2].toInt()
            try {
                dateTime = LocalDateTime(year, month, day, 0, 0)
                this.dueTag = getDueTag()
            } catch (e: DateTimeException) {
                println("The input date is invalid")
                isDateCorrect = false
            } catch (e: java.lang.IllegalArgumentException) {
                println("The input date is invalid")
                isDateCorrect = false
            }
        }
        return isDateCorrect
    }

    fun setTime(inputString: String): Boolean {
        var isTimeCorrect = false
        val time = inputString.trim().lowercase()

        isTimeCorrect = time.matches(Regex("\\d{1,2}:\\d{1,2}"))
        if (!isTimeCorrect) {
            println("The input time is invalid")
        } else {
            val split = time.split(":")
            try {
                val hour = split[0].toInt()
                val minutes = split[1].toInt()
                val tmp = LocalDateTime(dateTime!!.year, dateTime!!.month, dateTime!!.dayOfMonth, hour, minutes)
                dateTime = tmp
            } catch (e: DateTimeException) {
                isTimeCorrect = false
                println("The input time is invalid")
            } catch (e: java.lang.IllegalArgumentException) {
                isTimeCorrect = false
                println("The input time is invalid")
            }
        }

        return isTimeCorrect
    }

    fun setId(id: Int) {
        this.id = id
        this.id = this.id!! + 1
    }

    internal fun getDueTag(): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(dateTime!!.date)

        return if (numberOfDays == 0) {
            "T"
        } else if (numberOfDays > 0) {
            "I"
        } else {
            "O"
        }
    }

    fun getTaskDivider(): String {
        var divider = "  "

        if (id!! >= 10) {
            divider = " "
        }

        return divider
    }

    fun getTaskSubDivider(): String {
        var subDivider = "   "

        if (id!! >= 10) {
            subDivider = "   "
        }

        return subDivider
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        val divider = this.getTaskDivider()
        val subDivider = this.getTaskSubDivider()

        val string = dateTime.toString().replace("T", " ")
        stringBuilder.appendLine("${this.id}$divider$string $priority ${this.dueTag}")

        for (task in buffer) {
            stringBuilder.appendLine("$subDivider${task}")
        }

        return stringBuilder.toString()
    }

}

class TaskAdder {
    private val PRIORITY_INPUT = "Input the task priority (C, H, N, L):"
    private val DATE_INPUT = "Input the date (yyyy-mm-dd):"
    private val TIME_INPUT = "Input the time (hh:mm):"

    fun add() {
        val task = Task()

        setPriority(task)

        setDate(task)

        setTime(task)

        task.setId(tasks.size)

        setTaskBuffer(task)

        tasks.add(task)
    }

    fun setTaskBuffer(task: Task) {
        println("Input a new task (enter a blank line to end):")
        val buffer = mutableListOf<String>()

        while (true) {
            val taskInput = readln().trim()

            if (taskInput == "") {
                if (buffer.size == 0) {
                    println("The task is blank")
                } else {
                    task.buffer = buffer
                }
                break
            }

            buffer.add(taskInput)
        }
    }

    fun setTime(task: Task) {
        var isTimeCorrect = false
        while (!isTimeCorrect) {
            println(TIME_INPUT)
            isTimeCorrect = task.setTime(readln())
        }
    }

    fun setDate(task: Task) {
        var isDateCorrect = false
        while (!isDateCorrect) {
            println(DATE_INPUT)
            isDateCorrect = task.setDate(readln())

        }
    }

    fun setPriority(task: Task) {
        var isCorrectPriority = false
        while (!isCorrectPriority) {
            println(PRIORITY_INPUT)
            isCorrectPriority = task.setPriority(readln())
        }
    }
}

class TaskDeleter {
    fun delete(index: Int): Boolean {
        val taskSelector = TaskSelector()
        var isDeleted = false

        val task = taskSelector.getTaskById(index)
        if (task != null) {
            isDeleted = tasks.remove(task)

            recalculateTaskNumbers()

        } else {
            println("No tasks have been input")
        }

        if (isDeleted) {
            println("The task is deleted")
        }

        return isDeleted
    }

    private fun recalculateTaskNumbers() {
        for ((index, task) in tasks.withIndex()) {
            task.setId(index)
        }
    }
}

class TaskEditor {
    val taskSelector: TaskSelector = TaskSelector()
    val taskAdder: TaskAdder = TaskAdder()
    var EDITABLE_FIELDS = listOf("priority", "date", "time", "task")

    fun edit(index: Int): Boolean {
        var isTaskChanged = false
        var isCorrectField = false

        val task = taskSelector.getTaskById(index)

        if (task != null) {

            var field = ""
            while (!isCorrectField) {
                println("Input a field to edit (priority, date, time, task):")
                field = readln().trim().lowercase()
                isCorrectField = EDITABLE_FIELDS.contains(field)
                if (!isCorrectField) {
                    println("Invalid field")
                }
            }

            when (field) {
                "priority" -> {
                    taskAdder.setPriority(task)
                    isTaskChanged = true
                }

                "date" -> {
                    taskAdder.setDate(task)
                    isTaskChanged = true
                }

                "time" -> {
                    taskAdder.setTime(task)
                    isTaskChanged = true
                }

                "task" -> {
                    taskAdder.setTaskBuffer(task)
                    isTaskChanged = true
                }

                else -> {
                    isTaskChanged = false
                    println("Invalid field")
                }
            }
        } else {
            println("No tasks have been input")
        }

        if (isTaskChanged) {
            println("The task is changed")
        }

        return isTaskChanged
    }
}

class TaskSelector {
    fun select(): Int {
        var index = 0

        if (tasks.isEmpty()) {
            index = -1
        } else {
            var isValidTaskNumber = false
            while (!isValidTaskNumber) {
                println("Input the task number (1-${tasks.size}):")
                val taskNumberInput = readln().trim()
                isValidTaskNumber = this.isValidTaskNumber(taskNumberInput)
                if (isValidTaskNumber) {
                    index = taskNumberInput.toInt()
                } else {
                    println("Invalid task number")
                }
            }
        }

        return index
    }

    private fun isValidTaskNumber(taskNumber: String): Boolean {

        var isValidTaskNumber = false

        var toInt = 0

        try {
            toInt = taskNumber.toInt()

            if (toInt in 1..tasks.size) {
                isValidTaskNumber = true
            }
        } catch (e: NumberFormatException) {
            isValidTaskNumber = false
        }

        return isValidTaskNumber
    }

    fun getTaskById(index: Int): Task? {
        var result: Task? = null

        for (task in tasks) {
            if (task.id == index) {
                result = task
            }
        }

        return result
    }
}

class CommandProcessor {
    private var COMMAND_ADD = "add"
    private var COMMAND_PRINT = "print"
    private var COMMAND_EDIT = "edit"
    private var COMMAND_DELETE = "delete"
    private var COMMAND_END = "end"

    private var tasksPrinter: TasksPrinter = TasksPrinter()
    private var taskAdder: TaskAdder = TaskAdder()
    private var taskDeleter: TaskDeleter = TaskDeleter()
    private var taskEditor: TaskEditor = TaskEditor()
    private var taskSelector: TaskSelector = TaskSelector()

    var SUPPLIED_COMMANDS = listOf(COMMAND_ADD, COMMAND_PRINT, COMMAND_EDIT, COMMAND_DELETE, COMMAND_END)

    private fun isSuppliedCommand(x: String): Boolean {
        return SUPPLIED_COMMANDS.contains(x.lowercase())
    }

    fun process(input: String): Boolean {
        val command = input.trim()
        var result = false
        if (isSuppliedCommand(input)) {
            when (command) {
                COMMAND_ADD -> {
                    result = true
                    taskAdder.add()
                }

                COMMAND_PRINT -> {
                    result = true
                    if (tasks.isEmpty()) {
                        println("No tasks have been input")
                    } else {
                        tasksPrinter.printTasks()
                    }
                }

                COMMAND_EDIT -> {
                    result = true
                    tasksPrinter.printTasks()
                    val taskNumber: Int = taskSelector.select()
                    taskEditor.edit(taskNumber)
                }

                COMMAND_DELETE -> {
                    result = true
                    tasksPrinter.printTasks()
                    val taskNumber: Int = taskSelector.select()
                    taskDeleter.delete(taskNumber)
                }

                COMMAND_END -> {
                    println("Tasklist exiting!")
                }
            }

        } else {
            println("The input action is invalid")
            result = true
        }

        return result
    }
}

fun main() {
    val commandProcessor = CommandProcessor()
    while (true) {
        println("Input an action (${commandProcessor.SUPPLIED_COMMANDS.joinToString(", ")}):")
        val task = readln().trim()
        val commandResult = commandProcessor.process(task)

        if (!commandResult) {
            break
        }
    }
}


