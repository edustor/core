package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class Session() {
    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var user: Account

    val token: String = UUID.randomUUID().toString()

    @JsonIgnore val createdAt = Instant.now()
    @JsonIgnore var FCMToken: String? = null
    @Id var id: String = UUID.randomUUID().toString()

    constructor(user: Account) : this() {
        this.user = user
    }
}