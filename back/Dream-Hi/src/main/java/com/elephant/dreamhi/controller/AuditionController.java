package com.elephant.dreamhi.controller;

import com.elephant.dreamhi.exception.NotFoundException;
import com.elephant.dreamhi.model.dto.BookPeriod;
import com.elephant.dreamhi.security.PrincipalDetails;
import com.elephant.dreamhi.service.AuditionService;
import com.elephant.dreamhi.utils.Response;
import com.elephant.dreamhi.utils.Response.Body;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/announcements/{announcementId}/audition")
@RequiredArgsConstructor
public class AuditionController {

    private final AuditionService auditionService;

    /**
     * @param processId      오디션의 현재 절차 ID
     * @param announcementId 현재 공고 ID
     * @param producerId     현재 공고를 작성한 제작사 ID
     * @param user           현재 로그인 한 유저
     * @return 예약 가능한 날짜의 시작일과 종료일을 Response의 Body에 담아서 반환
     */
    @GetMapping("/on/{processId}/period")
    @PreAuthorize("@checker.isLoginUser(#user) && @checker.hasPassedAuthority(#user, #producerId, #announcementId, #processId)")
    public ResponseEntity<Body> findBookPeriod(
            @PathVariable Long processId,
            @PathVariable Long announcementId,
            @RequestParam(name = "pid", required = false) Long producerId,
            @AuthenticationPrincipal PrincipalDetails user
    ) {
        BookPeriod bookPeriod = auditionService.findBookPeriod(processId);
        return Response.create(HttpStatus.OK, "예약 가능한 날짜의 시작일과 종료일을 조회했습니다.", bookPeriod);
    }

    /**
     * @param processId      오디션의 현재 절차 ID
     * @param announcementId 현재 공고 ID
     * @param producerId     현재 공고를 작성한 제작사 ID
     * @param date           선택한 예약 날짜
     * @param user           현재 로그인 한 유저
     * @return 지원자에게는 예약 현황을, 제작사에게는 각 예약 현황에 지원한 User의 ID를 Response의 Body에 담아서 반환
     */
    @GetMapping("/on/{processId}/schedules")
    @PreAuthorize("@checker.isLoginUser(#user) && @checker.hasPassedAuthority(#user, #producerId, #announcementId, #processId)")
    public ResponseEntity<Body> findAllBook(
            @PathVariable Long processId,
            @PathVariable Long announcementId,
            @RequestParam(name = "pid", required = false) Long producerId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal PrincipalDetails user
    ) {
        Object bookDtos;

        if (producerId == null) {
            bookDtos = auditionService.findAllBookForVolunteer(processId, date);
        } else {
            bookDtos = auditionService.findAllBookForProducer(processId, date);
        }

        return Response.create(HttpStatus.OK, "요청한 일자의 예약 정보를 조회했습니다.", bookDtos);
    }

    /**
     * @param processId      오디션의 현재 절차 ID
     * @param announcementId 현재 공고 ID
     * @param producerId     현재 공고를 작성한 제작사 ID
     * @param user           현재 로그인 한 유저
     * @return 화상 오디션을 위한 파일 URL 조회 결과를 Response의 Body에 담아서 반환
     * @throws NotFoundException 현재 절차 ID에 대하여 사전에 저장해 둔 세션 정보가 없을 때 발생하는 예외
     */
    @GetMapping("/on/{processId}/file")
    @PreAuthorize("@checker.isLoginUser(#user) && @checker.hasPassedAuthority(#user, #producerId, #announcementId, #processId)")
    public ResponseEntity<Body> findFileUrl(
            @PathVariable Long processId,
            @PathVariable Long announcementId,
            @RequestParam(name = "pid", required = false) Long producerId,
            @AuthenticationPrincipal PrincipalDetails user
    ) throws NotFoundException {
        String fileUrl = auditionService.findFileUrl(processId);
        return Response.create(HttpStatus.OK, "화상 오디션을 위한 파일 URL를 조회했습니다.", fileUrl);
    }

    /**
     * @param processId 오디션의 현재 절차 ID
     * @param now       사용자의 현재 시간
     * @param user      현재 로그인한 유저
     * @return 화상 오디션에 사용할 세션 ID를 Response의 Body에 담아서 반환
     * @throws NotFoundException 현재 절차 ID에 대하여 사전에 저장해 둔 세션 정보가 없을 때 발생하는 예외
     */
    @GetMapping("/on/{processId}/session")
    @PreAuthorize("@checker.isLoginUser(#user) && @checker.hasBookAuthority(#user, #producerId, #announcementId, #processId, #now)")
    public ResponseEntity<Body> findSessionId(
            @PathVariable Long processId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime now,
            @PathVariable Long announcementId,
            @RequestParam(required = false) Long producerId,
            @AuthenticationPrincipal PrincipalDetails user
    ) throws NotFoundException {
        String sessionId = auditionService.findSessionId(processId);
        return Response.create(HttpStatus.OK, "화상 오디션 입장을 위한 세션 ID를 조회했습니다.", sessionId);
    }

    /**
     * @param processId 오디션의 현재 절차 ID, stage가 화상 오디션이어야 한다.
     * @param fileUrl   화상 오디션을 위한 참고 파일을 저장해 둔 URL
     * @param user      현재 로그인 한 유저
     * @return 화상 오디션에 대한 세션 정보를 저장하는 데 성공한 경우
     * @throws NotFoundException        오디션의 현재 절차를 조회할 수 없을 때 발생하는 예외
     * @throws IllegalArgumentException 현재 절차의 stage가 화상 오디션이 아닐 때 발생하는 예외
     */
    @PostMapping("/on/{processId}")
    @PreAuthorize("@checker.isLoginUser(#user) && @checker.hasAnnouncementAuthority(#user, #producerId, #announcementId)")
    public ResponseEntity<Body> saveSession(
            @PathVariable Long processId,
            @RequestBody String fileUrl,
            @PathVariable Long announcementId,
            @RequestBody Long producerId,
            @AuthenticationPrincipal PrincipalDetails user
    ) throws NotFoundException, IllegalArgumentException {
        auditionService.saveSession(processId, fileUrl);
        return Response.create(HttpStatus.ACCEPTED, "화상 오디션을 위한 세션 정보를 저장했습니다.");
    }

}
