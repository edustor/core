package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
class Session() {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    lateinit var user: Account

    @Column(nullable = false)
    val token: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    @JsonIgnore val createdAt = Instant.now()
    @JsonIgnore var FCMToken: String? = null
    @Id var id: String = UUID.randomUUID().toString()

    constructor(user: Account) : this() {
        this.user = user
    }
}