package com.injecapstone.capstonebackend.quiz.service;

import com.injecapstone.capstonebackend.ai.client.AiClient;
import com.injecapstone.capstonebackend.ai.dto.AiOption;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestion;
import com.injecapstone.capstonebackend.ai.dto.AiQuizQuestionDetail;
import com.injecapstone.capstonebackend.ai.dto.AiTodayRules;
import com.injecapstone.capstonebackend.quiz.domain.ViewedTerm;
import com.injecapstone.capstonebackend.quiz.dto.ViewedRulesRequest;
import com.injecapstone.capstonebackend.quiz.dto.ViewedRulesResponse;
import com.injecapstone.capstonebackend.user.domain.LearningLevel;
import com.injecapstone.capstonebackend.global.error.BusinessException;
import com.injecapstone.capstonebackend.global.error.ErrorCode;
import com.injecapstone.capstonebackend.quiz.domain.QuizAttempt;
import com.injecapstone.capstonebackend.quiz.dto.QuizAnswerResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizAttemptDetailResponse;
import com.injecapstone.capstonebackend.quiz.dto.QuizQuestionResponse;
import com.injecapstone.capstonebackend.quiz.repository.QuizAttemptRepository;
import com.injecapstone.capstonebackend.quiz.repository.ViewedTermRepository;
import com.injecapstone.capstonebackend.user.domain.User;
import com.injecapstone.capstonebackend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LearningQuizServiceTest {

    private static final List<AiOption> OPTIONS = List.of(
            new AiOption(1, "보크"), new AiOption(2, "도루"),
            new AiOption(3, "번트"), new AiOption(4, "병살")
    );

    private final UserRepository userRepository = mock(UserRepository.class);
    private final QuizAttemptRepository attemptRepository = mock(QuizAttemptRepository.class);
    private final AiClient ai = mock(AiClient.class);
    private final ViewedTermRepository viewedTermRepository = mock(ViewedTermRepository.class);
    private final User user = mock(User.class);

    private LearningQuizService service;

    @BeforeEach
    void setUp() {
        when(ai.enabled()).thenReturn(true);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(attemptRepository.save(any(QuizAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new LearningQuizService(userRepository, attemptRepository, ai, viewedTermRepository);
    }

    private AiQuizQuestionDetail detail(int questionId, int correctOptionId) {
        return new AiQuizQuestionDetail(
                questionId, "balk", "보크는?", OPTIONS, correctOptionId, "보크 해설"
        );
    }

    @Test
    void 오늘_본_용어로_AI_문항을_받는다() {
        String seed = "7-" + LocalDate.now(ZoneId.of("Asia/Seoul"));
        when(viewedTermRepository.findTermIds(7L, LocalDate.now(ZoneId.of("Asia/Seoul"))))
                .thenReturn(List.of("balk"));
        when(ai.quizQuestions(5, seed, List.of("balk"))).thenReturn(List.of(
                new AiQuizQuestion(1001, "balk", "보크는?", OPTIONS)
        ));

        List<QuizQuestionResponse> questions = service.getQuestions(7L);

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).questionId()).isEqualTo(1001);
        assertThat(questions.get(0).options()).extracting("text")
                .containsExactly("보크", "도루", "번트", "병살");
    }

    @Test
    void 오늘_본_용어가_없으면_퀴즈가_없다() {
        when(viewedTermRepository.findTermIds(any(), any())).thenReturn(List.of());

        assertThat(service.getQuestions(7L)).isEmpty();
        org.mockito.Mockito.verify(ai, org.mockito.Mockito.never())
                .quizQuestions(org.mockito.ArgumentMatchers.anyInt(), any(), any());
    }

    @Test
    void 시청_기록은_새로_본_용어만_저장하고_오늘_본_용어_전체를_돌려준다() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        when(user.getLearningLevel()).thenReturn(null);
        when(ai.todayRules("G1", 600, "INTRODUCTORY"))
                .thenReturn(Optional.of(new AiTodayRules(List.of("balk", "walk", "Bad Id", "balk"))));
        when(viewedTermRepository.findTermIds(7L, today))
                .thenReturn(List.of("walk"))
                .thenReturn(List.of("walk", "balk"));

        ViewedRulesResponse response = service.recordViewed(7L, new ViewedRulesRequest("G1", 600));

        ArgumentCaptor<ViewedTerm> saved = ArgumentCaptor.forClass(ViewedTerm.class);
        verify(viewedTermRepository).save(saved.capture());
        assertThat(saved.getValue().getTermId()).isEqualTo("balk");
        assertThat(saved.getValue().getGameId()).isEqualTo("G1");
        assertThat(saved.getValue().getViewedDate()).isEqualTo(today);
        assertThat(response.termIds()).containsExactly("walk", "balk");
        assertThat(response.date()).isEqualTo(today);
    }

    @Test
    void 시청_기록은_학습_수준대로_AI에_묻고_없는_경기는_거부() {
        when(user.getLearningLevel()).thenReturn(LearningLevel.FAMILIAR);
        when(ai.todayRules("NOPE", 10, "FAMILIAR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordViewed(7L, new ViewedRulesRequest("NOPE", 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void 동시에_같은_용어가_저장되면_건너뛴다() {
        when(ai.todayRules("G1", 1, "INTRODUCTORY"))
                .thenReturn(Optional.of(new AiTodayRules(List.of("balk"))));
        when(viewedTermRepository.findTermIds(any(), any())).thenReturn(List.of());
        when(viewedTermRepository.save(any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("dup"));

        assertThat(service.recordViewed(7L, new ViewedRulesRequest("G1", 1)).termIds()).isEmpty();
    }

    @Test
    void AI_정답으로_채점하고_기록한다() {
        when(ai.quizQuestion(1001)).thenReturn(Optional.of(detail(1001, 1)));

        QuizAnswerResponse right = service.grade(7L, 1001, 1);
        QuizAnswerResponse wrong = service.grade(7L, 1001, 3);

        assertThat(right.correct()).isTrue();
        assertThat(wrong.correct()).isFalse();
        assertThat(wrong.correctOptionId()).isEqualTo(1);
        assertThat(wrong.explanation()).isEqualTo("보크 해설");

        ArgumentCaptor<QuizAttempt> saved = ArgumentCaptor.forClass(QuizAttempt.class);
        verify(attemptRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting("questionId").containsExactly(1001, 1001);
    }

    @Test
    void 없는_문항이나_없는_보기는_거부() {
        when(ai.quizQuestion(9999)).thenReturn(Optional.empty());
        when(ai.quizQuestion(1001)).thenReturn(Optional.of(detail(1001, 1)));

        assertThatThrownBy(() -> service.grade(7L, 9999, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT);
        assertThatThrownBy(() -> service.grade(7L, 1001, 5))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void AI로_바꾸기_전_임시_문항_풀이_기록도_보인다() {
        QuizAttempt legacy = QuizAttempt.create(user, 1, 1, true);
        when(attemptRepository.findByIdAndUser_Id(3L, 7L)).thenReturn(Optional.of(legacy));
        when(ai.quizQuestion(1)).thenReturn(Optional.empty());

        Optional<QuizAttemptDetailResponse> detail = service.getMyAttempt(7L, 3L);

        assertThat(detail).hasValueSatisfying(item -> {
            assertThat(item.questionText()).contains("다음 베이스");
            assertThat(item.correctOptionId()).isEqualTo(1);
        });
    }

    @Test
    void AI_문항_풀이_기록_상세는_AI_정답과_해설() {
        QuizAttempt attempt = QuizAttempt.create(user, 1001, 3, false);
        when(attemptRepository.findByIdAndUser_Id(4L, 7L)).thenReturn(Optional.of(attempt));
        when(ai.quizQuestion(1001)).thenReturn(Optional.of(detail(1001, 1)));

        QuizAttemptDetailResponse detail = service.getMyAttempt(7L, 4L).orElseThrow();

        assertThat(detail.questionText()).isEqualTo("보크는?");
        assertThat(detail.correctOptionId()).isEqualTo(1);
        assertThat(detail.explanation()).isEqualTo("보크 해설");
        assertThat(detail.options()).hasSize(4);
    }

    @Test
    void AI를_끈_뒤_AI_문항_기록_상세는_503() {
        LearningQuizService local = new LearningQuizService(
                userRepository, attemptRepository, AiClient.disabled(), viewedTermRepository
        );
        QuizAttempt attempt = QuizAttempt.create(user, 1005, 1, true);
        when(attemptRepository.findByIdAndUser_Id(5L, 7L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> local.getMyAttempt(7L, 5L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    @Test
    void AI_주소가_없으면_임시_문항() {
        LearningQuizService local = new LearningQuizService(
                userRepository, attemptRepository, AiClient.disabled(), viewedTermRepository
        );

        assertThat(local.getQuestions(7L)).extracting("questionId").containsExactly(1);
        assertThat(local.grade(7L, 1, 1).correct()).isTrue();
    }
}
