package com.example.vjutest.Service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.example.vjutest.Repository.AnswerRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.DTO.AnswerDTO; // Replace with the correct package of AnswerDTO
import com.example.vjutest.Mapper.AnswerMapper;
import com.example.vjutest.Model.Answer;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnswerService {
    
    private final AnswerMapper answerMapper; // Assuming you have an AnswerMapper
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public List<AnswerDTO> createAnswersForQuestion(Long questionId, Long userId, List<AnswerDTO> answerRequest) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại!"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (!question.getCreatedBy().equals(user) && !user.getRole().getName().equals("admin")) {
            throw new RuntimeException("Bạn không có quyền tạo đáp án!");
        }
            
        if (answerRequest.size() > 4) {
            throw new RuntimeException("Chỉ được tạo tối đa 4 đáp án cho mỗi câu hỏi.");
        }

        long countCorrect = answerRequest.stream().filter(AnswerDTO::getIsCorrect).count();
        if (countCorrect != 1) {
            throw new RuntimeException("Phải có duy nhất một đáp án đúng.");
        }

        // Kiểm tra xem câu hỏi đã có đáp án chưa
        if (!answerRepository.findByQuestionId(questionId).isEmpty()) {
            throw new RuntimeException("Câu hỏi này đã có đáp án!");
        }

        List<Answer> answers = answerRequest.stream().map(dto -> {
            Answer answer = new Answer();
            answer.setAnswerName(dto.getAnswerName());
            answer.setIsCorrect(dto.getIsCorrect());
            answer.setQuestion(question);
            answer.setCreatedBy(user);
            answer.setModifiedBy(user);
            return answer;
        }).toList();

        List<Answer> saved = answerRepository.saveAll(answers);
        return saved.stream().map(answerMapper::toSimpleDTO).toList();
    }
}

