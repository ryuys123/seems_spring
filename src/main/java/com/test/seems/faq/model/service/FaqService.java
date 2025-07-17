package com.test.seems.faq.model.service;

import com.test.seems.faq.jpa.entity.FaqEntity;
import com.test.seems.faq.jpa.repository.FaqRepository;
import com.test.seems.faq.model.dto.Faq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j   // Logger 객체 선언임, 별도의 로그 객체 생성구문 필요없음, 레퍼런스는 log 임
@Service
@RequiredArgsConstructor
@Transactional
public class FaqService  {
    // jpa 가 제공하는 기본 메소드를 사용하려면
    @Autowired
    private final FaqRepository faqRepository;

    // ArrayList<Faq> 리턴하는 메소드들이 사용하는 중복 코드는 별도의 메소드로 작성함
    private ArrayList<Faq> toList(Page<FaqEntity> page) {
        ArrayList<Faq> list = new ArrayList<>();
        for (FaqEntity faqEntity : page) {
            list.add(faqEntity.toDto());
        }
        return list;
    }

    public int selectListCount() {
        return (int)faqRepository.count();
    }

    public ArrayList<Faq> selectList(Pageable pageable) {
        return toList(faqRepository.findAll(pageable));
    }

    public Faq selectFaq(int faqNo) {
        // jpa 제공 메소드 사용
        // findById(id) : Optional<T>
        // 엔티티에 등록된 id 를 사용해서 entity 1개를 조회함
        Optional<FaqEntity> entityOptional = faqRepository.findById(faqNo);
        return entityOptional.get().toDto();
    }

    public Faq selectLast() {
        // 추가 메소드로 작성
		/* sql :
		*	select * from notice
			where faqno = (select max(noticeno) from notice)
		* */
        Optional<FaqEntity> entityOptional = faqRepository.findTopByOrderByFaqNoDesc();
        return entityOptional.isPresent() ? entityOptional.get().toDto() : null;
    }

    public int insertFaq(Faq faq) {
        // jpa 가 제공하는 메소드 사용
        // save(entity) : entity
        // 성공하면 기록한 entity 가 리턴되고, 실패하면 null 리턴됨
        FaqEntity savedEntity = faqRepository.save(faq.toEntity());
        return savedEntity != null ? 1 : 0;
    }

    public int deleteFaq(int faqNo) {
        // jpa 가 제공하는 메소드 사용
        // deleteById(pk 로 지정된 컬럼에 대한 property) : void
        // 성공하면 리턴값 없음, 실패하면 에러 발생함
        try {
            faqRepository.deleteById(faqNo);
            return 1;
        }catch(Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    public int updateFaq(Faq faq) {
        // jpa 가 제공하는 메소드 사용
        // save(entity) : savedEntity
        // 실패하면 null 리턴

        // faq번호가 존재하면 update, 존재하지 않으면 실패로 간주함
        if (faq.getFaqNo() == 0 || !faqRepository.existsById(faq.getFaqNo())) {
            return 0;
        }

        //대상 faq번호가 존재하면 수정 처리함
        FaqEntity updatedEntity = faqRepository.save(faq.toEntity());
        return updatedEntity != null ? 1 : 0;

    }
}
