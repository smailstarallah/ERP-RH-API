package ma.digitalia.generationfichepaie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryRangeDto {
    private int moins_5000;
    private int _5000_8000;
    private int _8000_12000;
    private int _12000_20000;
    private int plus_20000;
}
