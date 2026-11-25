package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.reaction.CreateReaction;
import dkhoa.chemistrylabvr.dto.request.reaction.ReactionComponentRequest;
import dkhoa.chemistrylabvr.dto.request.reaction.ReactionCondition;
import dkhoa.chemistrylabvr.dto.response.reaction.ReactionComponent;
import dkhoa.chemistrylabvr.dto.response.reaction.ReactionList;
import dkhoa.chemistrylabvr.dto.response.reaction.ReactionVisualEffects;
import dkhoa.chemistrylabvr.entity.Chemical;
import dkhoa.chemistrylabvr.entity.Reaction;
import dkhoa.chemistrylabvr.entity.ReactionComponentEntity;
import dkhoa.chemistrylabvr.enums.PhaseChange;
import dkhoa.chemistrylabvr.enums.ReactionComponentType;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.ChemicalRepository;
import dkhoa.chemistrylabvr.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final ChemicalRepository chemicalRepository;
    private final ChemicalEquationBalanceValidator balanceValidator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ReactionList> getAll() {
        return reactionRepository.findAllByIsDeletedFalseOrderByIdDesc()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReactionList getOne(Long id) {
        if (id == null) throw new AppException("ID phản ứng không được để trống", HttpStatus.BAD_REQUEST);
        Reaction reaction = reactionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException("Phản ứng không tồn tại", HttpStatus.NOT_FOUND));
        return toResponse(reaction);
    }

    @Transactional
    public ReactionList create(CreateReaction dto) {
        PreparedComponents prepared = prepare(dto);
        Reaction reaction = new Reaction();
        apply(reaction, dto, prepared);
        reaction.setCreatedAt(Instant.now());
        reaction.setUpdatedAt(Instant.now());
        reaction.setIsDeleted(false);
        reaction.setComponents(buildComponents(reaction, prepared));
        syncLegacyJson(reaction, prepared);
        return toResponse(reactionRepository.saveAndFlush(reaction));
    }

    @Transactional
    public ReactionList update(Long id, CreateReaction dto) {
        Reaction reaction = reactionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException("Phản ứng không tồn tại", HttpStatus.NOT_FOUND));
        PreparedComponents prepared = prepare(dto);
        apply(reaction, dto, prepared);
        reaction.setUpdatedAt(Instant.now());
        reaction.getComponents().clear();
        syncLegacyJson(reaction, prepared);
        reactionRepository.saveAndFlush(reaction);
        reaction.getComponents().addAll(buildComponents(reaction, prepared));
        return toResponse(reactionRepository.saveAndFlush(reaction));
    }

    @Transactional
    public void delete(Long id) {
        Reaction reaction = reactionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException("Phản ứng không tồn tại", HttpStatus.NOT_FOUND));
        reaction.setIsDeleted(true);
        reaction.setUpdatedAt(Instant.now());
        reactionRepository.save(reaction);
    }

    private void apply(Reaction reaction, CreateReaction dto, PreparedComponents prepared) {
        reaction.setEquation(buildEquation(dto, prepared));
        reaction.setDescription(dto.getDescription().trim());
        reaction.setIsReversible(dto.isReversible());
        reaction.setSourceReference(trimToNull(dto.getSourceReference()));
        reaction.setConditionsJson(conditionMap(dto.getConditions()));
        reaction.setVisualEffectsJson(objectMapper.writeValueAsString(visualEffects(prepared.products())));
    }

    private PreparedComponents prepare(CreateReaction dto) {
        if (dto.getReactants() == null || dto.getReactants().isEmpty()) {
            throw new AppException("Phải có ít nhất một chất phản ứng", HttpStatus.BAD_REQUEST);
        }
        if (dto.getProducts() == null || dto.getProducts().isEmpty()) {
            throw new AppException("Phải có ít nhất một sản phẩm", HttpStatus.BAD_REQUEST);
        }

        ensureNoDuplicate(dto.getReactants(), "chất phản ứng");
        ensureNoDuplicate(dto.getProducts(), "sản phẩm");
        List<Long> catalystIds = dto.getCatalystChemicalIds() == null ? List.of() : dto.getCatalystChemicalIds();
        if (catalystIds.stream().anyMatch(Objects::isNull)) {
            throw new AppException("ID chất xúc tác không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (catalystIds.stream().distinct().count() != catalystIds.size()) {
            throw new AppException("Không thêm trùng chất xúc tác", HttpStatus.BAD_REQUEST);
        }

        Set<Long> ids = new java.util.HashSet<>();
        dto.getReactants().forEach(c -> ids.add(c.chemicalId()));
        dto.getProducts().forEach(c -> ids.add(c.chemicalId()));
        ids.addAll(catalystIds);
        Map<Long, Chemical> chemicalById = chemicalRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Chemical::getId, Function.identity()));
        if (chemicalById.size() != ids.size()) {
            throw new AppException("Có hóa chất không tồn tại trong hệ thống", HttpStatus.BAD_REQUEST);
        }
        if (chemicalById.values().stream().anyMatch(c -> Boolean.TRUE.equals(c.getIsDeleted()))) {
            throw new AppException("Không thể dùng hóa chất đã bị xóa mềm", HttpStatus.BAD_REQUEST);
        }

        List<PreparedComponent> reactants = prepareRows(dto.getReactants(), chemicalById, ReactionComponentType.REACTANT);
        List<PreparedComponent> products = prepareRows(dto.getProducts(), chemicalById, ReactionComponentType.PRODUCT);
        List<PreparedComponent> catalysts = new ArrayList<>();
        for (Long catalystId : catalystIds) {
            catalysts.add(new PreparedComponent(chemicalById.get(catalystId), ReactionComponentType.CATALYST,
                    1, catalysts.size() + 1, PhaseChange.NONE));
        }
        balanceValidator.requireBalanced(reactants, products);
        return new PreparedComponents(reactants, products, catalysts);
    }

    private List<PreparedComponent> prepareRows(List<ReactionComponentRequest> rows,
                                                Map<Long, Chemical> chemicalById,
                                                ReactionComponentType type) {
        List<PreparedComponent> result = new ArrayList<>();
        for (ReactionComponentRequest row : rows) {
            if (row == null || row.chemicalId() == null) {
                throw new AppException("Hãy chọn hóa chất cho từng dòng", HttpStatus.BAD_REQUEST);
            }
            if (row.coefficient() < 1) {
                throw new AppException("Hệ số phải là số nguyên dương", HttpStatus.BAD_REQUEST);
            }
            PhaseChange phaseChange = row.phaseChange() == null ? PhaseChange.NONE : row.phaseChange();
            result.add(new PreparedComponent(chemicalById.get(row.chemicalId()), type,
                    row.coefficient(), result.size() + 1, phaseChange));
        }
        return result;
    }

    private void ensureNoDuplicate(List<ReactionComponentRequest> rows, String label) {
        Set<Long> ids = new java.util.HashSet<>();
        for (ReactionComponentRequest row : rows) {
            if (row == null || row.chemicalId() == null) {
                throw new AppException("Hãy chọn hóa chất cho từng dòng", HttpStatus.BAD_REQUEST);
            }
            if (!ids.add(row.chemicalId())) {
                throw new AppException("Không thêm trùng " + label + "; hãy gộp hệ số", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private List<ReactionComponentEntity> buildComponents(Reaction reaction, PreparedComponents prepared) {
        List<ReactionComponentEntity> result = new ArrayList<>();
        addEntities(result, reaction, prepared.reactants());
        addEntities(result, reaction, prepared.products());
        addEntities(result, reaction, prepared.catalysts());
        return result;
    }

    private void addEntities(List<ReactionComponentEntity> target, Reaction reaction, List<PreparedComponent> rows) {
        for (PreparedComponent row : rows) {
            ReactionComponentEntity entity = new ReactionComponentEntity();
            entity.setReaction(reaction);
            entity.setChemical(row.chemical());
            entity.setComponentType(row.type());
            entity.setCoefficient(row.coefficient());
            entity.setDisplayOrder(row.displayOrder());
            entity.setPhaseChange(row.phaseChange());
            target.add(entity);
        }
    }

    private String buildEquation(CreateReaction dto, PreparedComponents prepared) {
        String left = equationSide(prepared.reactants());
        String right = equationSide(prepared.products());
        return left + (dto.isReversible() ? " ⇌ " : " → ") + right;
    }

    private String equationSide(List<PreparedComponent> components) {
        return components.stream()
                .map(c -> (c.coefficient() == 1 ? "" : c.coefficient() + " ") + c.chemical().getFormula())
                .collect(Collectors.joining(" + "));
    }

    private Map<String, Object> conditionMap(ReactionCondition condition) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (condition != null) {
            if (condition.temperatureC() != null) map.put("temperatureC", condition.temperatureC());
            if (condition.notes() != null && !condition.notes().isBlank()) map.put("notes", condition.notes().trim());
        }
        return map;
    }

    private ReactionCondition conditionFrom(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return new ReactionCondition(null, null);
        Object temperature = map.get("temperatureC");
        BigDecimal temperatureC = temperature == null ? null : new BigDecimal(temperature.toString());
        Object notes = map.get("notes");
        return new ReactionCondition(temperatureC, notes == null ? null : notes.toString());
    }

    private ReactionVisualEffects visualEffects(Collection<PreparedComponent> products) {
        return new ReactionVisualEffects(
                products.stream().anyMatch(p -> p.phaseChange() == PhaseChange.GAS),
                products.stream().anyMatch(p -> p.phaseChange() == PhaseChange.PRECIPITATE)
        );
    }

    private ReactionList toResponse(Reaction reaction) {
        List<ReactionComponentEntity> rows = reaction.getComponents() == null ? List.of() : reaction.getComponents();
        List<ReactionComponent> reactants = rows.isEmpty()
                ? legacyComponents(reaction.getReactantsJson())
                : mapComponents(rows, ReactionComponentType.REACTANT);
        List<ReactionComponent> products = rows.isEmpty()
                ? legacyComponents(reaction.getProductsJson())
                : mapComponents(rows, ReactionComponentType.PRODUCT);
        List<ReactionComponent> catalysts = rows.isEmpty()
                ? List.of()
                : mapComponents(rows, ReactionComponentType.CATALYST);
        ReactionVisualEffects effects = rows.isEmpty()
                ? visualEffectsFromLegacy(reaction.getVisualEffectsJson())
                : new ReactionVisualEffects(
                    products.stream().anyMatch(c -> c.phaseChange() == PhaseChange.GAS),
                    products.stream().anyMatch(c -> c.phaseChange() == PhaseChange.PRECIPITATE));
        return new ReactionList(reaction.getId(), reaction.getEquation(), reactants, products, catalysts,
                conditionFrom(reaction.getConditionsJson()), effects, reaction.getDescription(),
                Boolean.TRUE.equals(reaction.getIsReversible()), reaction.getSourceReference(),
                reaction.getCreatedAt(), reaction.getUpdatedAt());
    }

    private List<ReactionComponent> mapComponents(List<ReactionComponentEntity> rows, ReactionComponentType type) {
        return rows.stream().filter(row -> row.getComponentType() == type)
                .sorted(java.util.Comparator.comparing(ReactionComponentEntity::getDisplayOrder))
                .map(row -> responseComponent(row.getChemical(), row.getCoefficient(), row.getPhaseChange()))
                .toList();
    }

    private ReactionComponent responseComponent(Chemical chemical, int coefficient, PhaseChange phaseChange) {
        return new ReactionComponent(chemical.getId(), chemical.getFormula(), coefficient, chemical.getState(),
                chemical.getUnityAssetId(), phaseChange == null ? PhaseChange.NONE : phaseChange);
    }

    private List<ReactionComponent> legacyComponents(String json) {
        if (json == null || json.isBlank()) return List.of();
        JsonNode root = objectMapper.readTree(json);
        if (root == null || !root.isArray()) return List.of();
        List<ReactionComponent> result = new ArrayList<>();
        for (JsonNode row : root) {
            JsonNode idNode = row.hasNonNull("chemicalId") ? row.get("chemicalId") : row.get("chemical_id");
            Long id = idNode == null || idNode.isNull() ? null : idNode.asLong();
            Chemical chemical = id == null ? null : chemicalRepository.findById(id).orElse(null);
            String formula = row.hasNonNull("formula") ? row.get("formula").asText()
                    : chemical == null ? null : chemical.getFormula();
            JsonNode coefficientNode = row.hasNonNull("coefficient") ? row.get("coefficient") : row.get("ratio");
            int coefficient = coefficientNode == null || !coefficientNode.canConvertToInt() ? 1 : coefficientNode.asInt();
            PhaseChange phaseChange = PhaseChange.NONE;
            if (row.hasNonNull("phaseChange")) phaseChange = phaseChange(row.get("phaseChange").asText());
            else if (row.hasNonNull("phase_change")) phaseChange = phaseChange(row.get("phase_change").asText());
            result.add(new ReactionComponent(id, formula, coefficient,
                    chemical == null ? null : chemical.getState(),
                    chemical == null ? null : chemical.getUnityAssetId(), phaseChange));
        }
        return result;
    }

    private ReactionVisualEffects visualEffectsFromLegacy(String json) {
        if (json == null || json.isBlank()) return new ReactionVisualEffects(false, false);
        JsonNode node = objectMapper.readTree(json);
        if (node == null || !node.isObject()) return new ReactionVisualEffects(false, false);
        JsonNode config = node.has("unityConfig") ? node.get("unityConfig") : node;
        return new ReactionVisualEffects(config.path("hasBubbles").asBoolean(false),
                config.path("hasPrecipitate").asBoolean(false));
    }

    private void syncLegacyJson(Reaction reaction, PreparedComponents prepared) {
        reaction.setReactantsJson(objectMapper.writeValueAsString(toLegacyRows(prepared.reactants())));
        reaction.setProductsJson(objectMapper.writeValueAsString(toLegacyRows(prepared.products())));
    }

    private List<Map<String, Object>> toLegacyRows(List<PreparedComponent> components) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PreparedComponent component : components) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("chemical_id", component.chemical().getId());
            row.put("formula", component.chemical().getFormula());
            row.put("coefficient", component.coefficient());
            row.put("phaseChange", component.phaseChange().name());
            result.add(row);
        }
        return result;
    }

    private PhaseChange phaseChange(String value) {
        try {
            return PhaseChange.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PhaseChange.NONE;
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    record PreparedComponent(Chemical chemical, ReactionComponentType type, int coefficient,
                             int displayOrder, PhaseChange phaseChange) {}

    private record PreparedComponents(List<PreparedComponent> reactants, List<PreparedComponent> products,
                                      List<PreparedComponent> catalysts) {}
}
