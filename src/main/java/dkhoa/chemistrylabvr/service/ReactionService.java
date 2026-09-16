package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.entity.Reaction;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;

    public List<Reaction> getAll() {
        return reactionRepository.findAll();
    }

    public Reaction getOne(Long id) {
        return reactionRepository.findById(id)
                .orElseThrow(() -> new AppException("Phản ứng hóa học này không tồn tại!"));
    }

    public void create() {

    }
}
