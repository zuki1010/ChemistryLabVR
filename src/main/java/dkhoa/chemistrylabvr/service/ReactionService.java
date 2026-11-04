package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.response.reaction.ReactionComponent;
import dkhoa.chemistrylabvr.dto.response.reaction.ReactionList;
import dkhoa.chemistrylabvr.entity.Reaction;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final ObjectMapper objectMapper;
    private Reaction reaction;

    public List<ReactionList> getAll() {
        return reactionRepository.findAll()
                .stream()
                .map(this::toReactionList)
                .toList();
    }

    public ReactionList getOne(Long id) {
        Reaction reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new AppException("Phương trình phản ứng này không tồn tại!", HttpStatus.NOT_FOUND));
        return toReactionList(reaction);
    }

    public void create() {

    }

    private ReactionList toReactionList(Reaction reaction) {
        List<ReactionComponent> reactants =
                objectMapper.readValue(
                        reaction.getReactantsJson(),
                        new TypeReference<List<ReactionComponent>>() {
                        }
                );

        List<ReactionComponent> products =
                objectMapper.readValue(
                        reaction.getProductsJson(),
                        new TypeReference<List<ReactionComponent>>() {
                        }
                );
        return new ReactionList(
                reaction.getId(),
                reaction.getEquation(),
                reactants,
                products,
                reaction.getDescription()
        );
    }
}
