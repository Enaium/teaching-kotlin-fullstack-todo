package cn.enaium.service

import cn.enaium.entity.Task
import cn.enaium.entity.endTime
import cn.enaium.entity.startTime
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull

/**
 * @author Enaium
 */
class TodoServe(private val sql: KSqlClient) {
    fun getTasks(): List<Task> {
        return sql.createQuery(Task::class) {
            orderBy(table.endTime.isNotNull(), table.startTime)
            select(table)
        }.execute()
    }

    fun saveTask(task: Task) {
        sql.save(task)
    }
}