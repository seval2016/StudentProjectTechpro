package com.project.service.business;

import com.project.entity.concretes.business.EducationTerm;
import com.project.exception.BadRequestException;
import com.project.exception.ConflictException;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.mappers.EducationTermMapper;
import com.project.payload.messages.ErrorMessages;
import com.project.payload.messages.SuccessMessages;
import com.project.payload.request.business.EducationTermRequest;

import com.project.payload.response.business.EducationTermResponse;
import com.project.payload.response.business.ResponseMessage;
import com.project.repository.business.EducationTermRepository;
import com.project.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationTermService {

    private final EducationTermRepository educationTermRepository;
    private final EducationTermMapper educationTermMapper;
    private final PageableHelper pageableHelper;

    public ResponseMessage<EducationTermResponse> saveEducationTerm(EducationTermRequest educationTermRequest) {
        validateEducationTermDates(educationTermRequest);
        EducationTerm savedEducationTerm =
                educationTermRepository.save(educationTermMapper.mapEducationTermRequestToEducationTerm(educationTermRequest));

        return ResponseMessage.<EducationTermResponse>builder()
                .message(SuccessMessages.EDUCATION_TERM_SAVED)
                .object(educationTermMapper.mapEducationTermToEducationTermResponse(savedEducationTerm))
                .httpStatus(HttpStatus.CREATED)
                .build();

    }

    //!!! Yrd Methd -1
    private void validateEducationTermDatesForRequest(EducationTermRequest educationTermRequest){
        // bu metoddaa amacımız requestten gelen RegistrationDate,startDate ve endDate arasındaki tarih sırasına göre doğru mu setlemiş onu kontrol etmek
        // registration > start
        if(educationTermRequest.getLastRegistrationDate().isAfter(educationTermRequest.getStartDate())){
            throw new ResourceNotFoundException(ErrorMessages.EDUCATION_START_DATE_IS_EARLIER_THAN_LAST_REGISTRATION_DATE);
        }

        // educationTerm end > educationTerm start olamaz !!!
        if(educationTermRequest.getEndDate().isBefore(educationTermRequest.getStartDate())) {
            throw new ResourceNotFoundException(ErrorMessages.EDUCATION_END_DATE_IS_EARLIER_THAN_START_DATE);
        }
    }
  //!!! Yrd Method-2
  private void validateEducationTermDates(EducationTermRequest educationTermRequest){

      validateEducationTermDatesForRequest(educationTermRequest);

      // !!! Bir yil icinde bir tane Guz donemi veya Yaz Donemi olmali kontrolu
      if(educationTermRepository.existsByTermAndYear(educationTermRequest.getTerm(),
              educationTermRequest.getStartDate().getYear())){
          throw new ConflictException(ErrorMessages.EDUCATION_TERM_IS_ALREADY_EXIST_BY_TERM_AND_YEAR_MESSAGE);
      }
      // !!! yil icine eklencek educationTerm, mevcuttakilerin tarihleri ile cakismamali
      if(educationTermRepository.findByYear(educationTermRequest.getStartDate().getYear())
              .stream()
              .anyMatch(educationTerm ->
                      (         educationTerm.getStartDate().equals(educationTermRequest.getStartDate()) //!!! 1. kontrol : baslama tarihleri ayni ise --> et1(10 kasim 2023) / YeniEt(10 kasim 2023)
                              ||  (educationTerm.getStartDate().isBefore(educationTermRequest.getStartDate())//!!! 2. kontrol : baslama tarihi mevcuttun baslama ve bitis tarihi ortasinda ise -->
                              && educationTerm.getEndDate().isAfter(educationTermRequest.getStartDate())) // Ornek : et1 ( baslama 10 kasim 2023 - bitme 20 kasim 2023)  - YeniEt ( baslama 15 kasim 2023 bitme 25 kasim 2023)
                              ||  (educationTerm.getStartDate().isBefore(educationTermRequest.getEndDate())//!!! 3. kontrol bitis tarihi mevcuttun baslama ve bitis tarihi ortasinda ise
                              && educationTerm.getEndDate().isAfter(educationTermRequest.getEndDate())) // Ornek : et1 ( baslama 10 kasim 2023 - bitme 20 kasim 2023)  - YeniEt ( baslama 09 kasim 2023 bitme 15 kasim 2023)
                              ||  (educationTerm.getStartDate().isAfter(educationTermRequest.getStartDate()) //!!!4.kontrol : yeni eklenecek eskiyi tamamen kapsiyorsa
                              && educationTerm.getEndDate().isBefore(educationTermRequest.getEndDate()))//et1 ( baslama 10 kasim 2023 - bitme 20 kasim 2023)  - YeniEt ( baslama 09 kasim 2023 bitme 25 kasim 2023)

                      ))
      ){
          throw new BadRequestException(ErrorMessages.EDUCATION_TERM_CONFLICT_MESSAGE);
      }
  }

    public EducationTermResponse getEducationTermById(Long id) {
        EducationTerm term = isEducationTermExist(id);
        return educationTermMapper.mapEducationTermToEducationTermResponse(term);
    }

    private EducationTerm isEducationTermExist(Long id){
        return educationTermRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessages.EDUCATION_TERM_NOT_FOUND_MESSAGE,id)));
    }

    public List<EducationTermResponse> getAllEducationTerms() {

        return educationTermRepository.findAll()
                .stream()
                .map(educationTermMapper::mapEducationTermToEducationTermResponse)
                .collect(Collectors.toList());//list data türüne çeviriyoruz

        //Ödev : lambda olmadan bu methodu yazmaya calışalım
    }

    public Page<EducationTermResponse> getAllEducationTermByPage(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return educationTermRepository.findAll(pageable)
                .map(educationTermMapper::mapEducationTermToEducationTermResponse);
    }

    public ResponseMessage deleteEducationTermById(Long id) {

        isEducationTermExist(id);
        //!!! SORU : EducationTerm silinince LessonProgramlar ne olacak, buraya onuda sileecek
        // kodlar eklememiz gerekecek mi?? Hayir, EducationTerm entityde Cascade kullanildigi icin
        // gerek yok..
        educationTermRepository.deleteById(id);

        return ResponseMessage.builder()
                .message(SuccessMessages.EDUCATION_TERM_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<EducationTermResponse> updateEducationTerm(Long id, EducationTermRequest educationTermRequest) {

        // !!! id var mi ??
        isEducationTermExist(id);

        //TODO : ayni yil olunca hata aliyoruz

        //!!! tarihler arasında çakışma var mı ??
        validateEducationTermDates(educationTermRequest);
        EducationTerm educationTerm2 =
                educationTermMapper.mapEducationTermRequestToUpdatedEducationTerm(id,educationTermRequest);

        EducationTerm educationTermUpdated = educationTermRepository.save(educationTerm2);

        return ResponseMessage.<EducationTermResponse>builder()
                .message(SuccessMessages.EDUCATION_TERM_UPDATE)
                .httpStatus(HttpStatus.OK)
                .object(educationTermMapper.mapEducationTermToEducationTermResponse(educationTermUpdated))
                .build();
    }

    //!!! LessonProgram icin yazildi
    public EducationTerm findEducationTermById(Long id){
        return isEducationTermExist(id);
    }


}











