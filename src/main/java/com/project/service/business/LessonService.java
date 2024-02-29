package com.project.service.business;

import com.project.entity.concretes.business.Lesson;
import com.project.exception.ConflictException;
import com.project.payload.mappers.LessonMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.business.LessonRequest;
import com.project.payload.response.business.LessonResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.repository.business.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;

    public ResponseMessage<LessonResponse> saveLesson(LessonRequest lessonRequest) {

        //!!! lesson Name uniq mi ?
        isLessonExistByLessonName(lessonRequest.getLessonName());

        Lesson lesson = lessonMapper.mapLessonRequestToLesson(lessonRequest);
        //!!! DTO --> POJO
        Lesson savedLesson = lessonRepository.save(lesson);

        return ResponseMessage.<LessonResponse>builder()
                .message(SuccessMessages.LESSON_SAVE)
                .object(lessonMapper.mapLessonToLessonResponse(savedLesson))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    private boolean isLessonExistByLessonName(String lessonName){ // JAVA , java
        boolean lessonExist =  lessonRepository.existsLessonByLessonNameEqualsIgnoreCase(lessonName);

        if(lessonExist){
            throw new ConflictException(
                    String.format(ErrorMessages.ALREADY_EXIST_LESSON_WITH_LESSON_NAME_MESSAGE,lessonName));
        } else {
            return false;
        }
    }
}
