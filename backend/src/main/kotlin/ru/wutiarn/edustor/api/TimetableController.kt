package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.TimetableEntry
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.SubjectsRepository
import ru.wutiarn.edustor.repository.UserRepository
import ru.wutiarn.edustor.utils.extensions.hasAccess
import java.time.DayOfWeek
import java.time.LocalTime

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
        if (!user.hasAccess(subject)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN)

        val start = LocalTime.of(start_hour, start_minute)
        val end = LocalTime.of(end_hour, end_minute)

        if (start > end) throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Start time is greater than end time")

        val timetableEntry = TimetableEntry(subject, DayOfWeek.of(day_of_week), start, end)

        for (existEntry in user.timetable) {
            if (timetableEntry.isIntersects(existEntry)) throw HttpRequestProcessingException(HttpStatus.CONFLICT,
                    "New entry intersects with ${existEntry.subject?.name} on ${existEntry.start} - ${existEntry.end}")
        }

        user.timetable.add(timetableEntry)
        userRepository.save(user)

        return timetableEntry
    }

}
