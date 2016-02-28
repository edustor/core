package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exception.HttpRequestProcessingException
import ru.wutiarn.edustor.models.TimetableEntry
import ru.wutiarn.edustor.models.TimetableTime
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.SubjectsRepository
import ru.wutiarn.edustor.repository.UserRepository
import ru.wutiarn.edustor.utils.assertHasAccess

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/timetable")
class TimetableController @Autowired constructor(val userRepository: UserRepository,
                                                 val subjectsRepository: SubjectsRepository) {

    @RequestMapping("/list")
    fun listTimetable(@AuthenticationPrincipal user: User): List<TimetableEntry> {
        return user.timetable
    }

    @RequestMapping("/create")
    fun createTimetable(@AuthenticationPrincipal user: User,
                        @RequestParam("subject") subjectId: String,
                        @RequestParam day_of_week: Int,
                        @RequestParam start_hour: Int,
                        @RequestParam end_hour: Int,
                        @RequestParam start_minute: Int,
                        @RequestParam end_minute: Int
    ): TimetableEntry {
        val subject = subjectsRepository.findOne(subjectId) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        assertHasAccess(user, subject)

        // Spring, Kotlin, GREAT thanks. I don't see any other (concise) way to do that
        if (!((start_hour >= 0) and (start_hour <= 24) and (end_hour >= 0) and (end_hour <= 24)
                .and (start_minute >= 0) and (start_minute <= 60) and (end_minute >= 0) and (end_minute <= 60)))
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Incorrect hour or minute")

        val start = TimetableTime(start_hour, start_minute)
        val end = TimetableTime(end_hour, end_minute)


        if (start > end) throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Start time is greater than end time")

        val timetableEntry = TimetableEntry(subject, day_of_week, start, end)

        for (existEntry in user.timetable) {
            if (timetableEntry.isIntersects(existEntry)) throw HttpRequestProcessingException(HttpStatus.CONFLICT,
                    "New entry intersects with ${existEntry.subject?.name} on ${existEntry.start} - ${existEntry.end}")
        }

        user.timetable.add(timetableEntry)
        userRepository.save(user)

        return timetableEntry
    }

}
