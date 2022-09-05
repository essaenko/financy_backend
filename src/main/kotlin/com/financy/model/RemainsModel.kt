package com.financy.model

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.float
import org.ktorm.schema.int
import java.time.LocalDate

val Database.Remains get() = this.sequenceOf(RemainsSchema)

object RemainsSchema: Table<Remains>("t_remains") {
  val id = int("id").primaryKey().bindTo { it.id }
  val createdAt = date("created_at").bindTo { it.createdAt }
  val updatedAt = date("updated_at").bindTo { it.updatedAt }
  val payment = int("payment_id").bindTo { it.payment }
  val remains = float("remains").bindTo { it.remains }
}

interface Remains: Entity<Remains> {
  companion object: Entity.Factory<Remains>()

  val id: Int
  var createdAt: LocalDate
  val updatedAt: LocalDate?
  var payment: Int
  var remains: Float
}