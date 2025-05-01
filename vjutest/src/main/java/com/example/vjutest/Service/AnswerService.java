package com.example.vjutest.Service;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.vjutest.Repository.AnswerRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.UserRepository;
import com.example.vjutest.DTO.AnswerDTO;
import com.example.vjutest.Mapper.AnswerMapper;
import com.example.vjutest.Model.Answer;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnswerService {
    
    private final AnswerMapper answerMapper;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final CloudinaryService cloudinaryService;

    public List<AnswerDTO> createAnswersForQuestion(Long questionId, Long userId, List<AnswerDTO> answerRequest, List<MultipartFile> imageFiles) throws IOException {
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

        // Validate image files
        if (imageFiles != null) {
            if (imageFiles.size() > answerRequest.size()) {
                throw new RuntimeException("Số lượng ảnh không được vượt quá số lượng đáp án!");
            }
            
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        throw new RuntimeException("Chỉ được phép tải lên file ảnh!");
                    }
                }
            }
        }

        List<Answer> answers = new ArrayList<>();
        for (int i = 0; i < answerRequest.size(); i++) {
            AnswerDTO dto = answerRequest.get(i);
            Answer answer = new Answer();
            answer.setAnswerName(dto.getAnswerName());
            answer.setIsCorrect(dto.getIsCorrect());
            answer.setQuestion(question);
            answer.setCreatedBy(user);
            answer.setModifiedBy(user);

            // Handle image upload if exists
            if (imageFiles != null && i < imageFiles.size() && imageFiles.get(i) != null && !imageFiles.get(i).isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(imageFiles.get(i), "answers");
                answer.setImageUrl(imageUrl);
            }

            answers.add(answer);
        }

        List<Answer> saved = answerRepository.saveAll(answers);
        question.setIsCompleted(questionService.checkIfQuestionIsCompleted(question));
        questionRepository.save(question);
        return saved.stream().map(answerMapper::toSimpleDTO).toList();
    }

    @Transactional
    public AnswerDTO updateAnswer(Long id, AnswerDTO answerRequest, Long userId, Long questionId, MultipartFile imageFile) throws IOException {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đáp án"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại!"));
            
        if (!question.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new RuntimeException("Bạn không có quyền cập nhật đáp án này");
        }

        // Validate image file
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Chỉ được phép tải lên file ảnh!");
            }
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (answer.getImageUrl() != null) {
                cloudinaryService.deleteImage(answer.getImageUrl());
            }
            // Upload new image
            String imageUrl = cloudinaryService.uploadImage(imageFile, "answers");
            answer.setImageUrl(imageUrl);
        }

        answer.setAnswerName(answerRequest.getAnswerName());
        answer.setIsCorrect(answerRequest.getIsCorrect());
        answer.setModifiedBy(user);

        answer = answerRepository.save(answer);
        return answerMapper.toFullDTO(answer);
    }

    @Transactional
    public void deleteAnswer(Long id, Long userId) throws IOException {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đáp án"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!answer.getQuestion().getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new RuntimeException("Bạn không có quyền xóa đáp án này");
        }

        // Delete image from Cloudinary if exists
        if (answer.getImageUrl() != null) {
            cloudinaryService.deleteImage(answer.getImageUrl());
        }

        answerRepository.delete(answer);
    }

    public List<AnswerDTO> getAnswersByQuestionDTO(Long questionId) {
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        return answers.stream().map(answerMapper::toSimpleDTO).toList();
    }

    @Transactional
    public AnswerDTO createSingleAnswer(Long questionId, Long userId, AnswerDTO answerRequest, MultipartFile imageFile) throws IOException {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại!"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (!question.getCreatedBy().equals(user) && !user.getRole().getName().equals("admin")) {
            throw new RuntimeException("Bạn không có quyền tạo đáp án!");
        }

        // Kiểm tra số lượng đáp án hiện tại
        List<Answer> existingAnswers = answerRepository.findByQuestionId(questionId);
        if (existingAnswers.size() >= 4) {
            throw new RuntimeException("Chỉ được tạo tối đa 4 đáp án cho mỗi câu hỏi.");
        }

        // Kiểm tra đáp án đúng nếu đang thêm đáp án đúng
        if (answerRequest.getIsCorrect()) {
            boolean hasCorrectAnswer = existingAnswers.stream()
                .anyMatch(Answer::getIsCorrect);
            if (hasCorrectAnswer) {
                throw new RuntimeException("Đã có đáp án đúng, không thể thêm đáp án đúng khác");
            }
        }

        // Validate image file
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Chỉ được phép tải lên file ảnh!");
            }
        }

        Answer answer = new Answer();
        answer.setAnswerName(answerRequest.getAnswerName());
        answer.setIsCorrect(answerRequest.getIsCorrect());
        answer.setQuestion(question);
        answer.setCreatedBy(user);
        answer.setModifiedBy(user);

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile, "answers");
            answer.setImageUrl(imageUrl);
        }

        answer = answerRepository.save(answer);
        
        // Cập nhật trạng thái hoàn thành của câu hỏi
        question.setIsCompleted(questionService.checkIfQuestionIsCompleted(question));
        questionRepository.save(question);
        
        return answerMapper.toSimpleDTO(answer);
    }
}

