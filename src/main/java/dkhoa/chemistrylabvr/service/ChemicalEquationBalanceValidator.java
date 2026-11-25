package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class ChemicalEquationBalanceValidator {
    public void requireBalanced(List<ReactionService.PreparedComponent> reactants,
                                List<ReactionService.PreparedComponent> products) {
        Map<String, Long> left = countAtoms(reactants);
        Map<String, Long> right = countAtoms(products);
        if (!left.equals(right)) {
            throw new AppException("Phương trình chưa cân bằng nguyên tố. Hãy kiểm tra hệ số.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private Map<String, Long> countAtoms(List<ReactionService.PreparedComponent> components) {
        Map<String, Long> total = new TreeMap<>();
        for (ReactionService.PreparedComponent component : components) {
            Map<String, Long> atoms = parseFormula(component.chemical().getFormula());
            atoms.forEach((element, count) -> total.merge(element,
                    count * component.coefficient(), Long::sum));
        }
        return total;
    }

    private Map<String, Long> parseFormula(String formula) {
        if (formula == null || formula.isBlank()) throw invalidFormula(formula);
        Map<String, Long> total = new HashMap<>();
        String[] hydrateParts = formula.trim().split("[·•]");
        for (String part : hydrateParts) {
            String segment = part.trim();
            if (segment.isEmpty()) throw invalidFormula(formula);
            int multiplier = 1;
            int offset = 0;
            while (offset < segment.length() && Character.isDigit(segment.charAt(offset))) offset++;
            if (offset > 0) {
                multiplier = parsePositiveInt(segment.substring(0, offset), formula);
                segment = segment.substring(offset);
            }
            int[] index = {0};
            Map<String, Long> group = parseGroup(segment, index, '\0', formula);
            if (index[0] != segment.length()) throw invalidFormula(formula);
            int segmentMultiplier = multiplier;
            group.forEach((element, count) -> total.merge(element, count * segmentMultiplier, Long::sum));
        }
        return total;
    }

    private Map<String, Long> parseGroup(String formula, int[] index, char expectedClose, String original) {
        Map<String, Long> result = new HashMap<>();
        while (index[0] < formula.length()) {
            char current = formula.charAt(index[0]);
            if (current == ')' || current == ']') {
                if (expectedClose == current) {
                    index[0]++;
                    return result;
                }
                throw invalidFormula(original);
            }
            if (current == '(' || current == '[') {
                char close = current == '(' ? ')' : ']';
                index[0]++;
                Map<String, Long> nested = parseGroup(formula, index, close, original);
                int multiplier = readMultiplier(formula, index, original);
                nested.forEach((element, count) -> result.merge(element, count * multiplier, Long::sum));
                continue;
            }
            if (!Character.isUpperCase(current)) throw invalidFormula(original);
            StringBuilder element = new StringBuilder().append(current);
            index[0]++;
            if (index[0] < formula.length() && Character.isLowerCase(formula.charAt(index[0]))) {
                element.append(formula.charAt(index[0]++));
        }
            int multiplier = readMultiplier(formula, index, original);
            result.merge(element.toString(), (long) multiplier, Long::sum);
        }
        if (expectedClose != '\0') throw invalidFormula(original);
        return result;
    }

    private int readMultiplier(String formula, int[] index, String original) {
        int start = index[0];
        while (index[0] < formula.length() && Character.isDigit(formula.charAt(index[0]))) index[0]++;
        return start == index[0] ? 1 : parsePositiveInt(formula.substring(start, index[0]), original);
    }

    private int parsePositiveInt(String value, String original) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) return parsed;
        } catch (NumberFormatException ignored) {
            // Report the chemical formula in a user-facing validation error below.
        }
        throw invalidFormula(original);
    }

    private AppException invalidFormula(String formula) {
        return new AppException("Không thể kiểm tra cân bằng vì công thức '" + formula
                + "' không đúng định dạng phân tử hỗ trợ.", HttpStatus.BAD_REQUEST);
    }
}
