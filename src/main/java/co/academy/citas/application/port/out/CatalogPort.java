package co.academy.citas.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogPort {
    List<CatalogItem> eps(boolean activeOnly);
    CatalogItem saveEps(Long id, String code, String name, boolean active);
    List<PlanItem> plans(long epsId, boolean activeOnly);
    PlanItem savePlan(Long id, long epsId, String code, String name, boolean active);
    List<CatalogItem> regimes(boolean activeOnly);
    CatalogItem saveRegime(String code, String name, boolean active);
    CatalogItem changeRegimeActive(String code, boolean active);
    List<SpecialtyItem> specialties(boolean activeOnly);
    SpecialtyItem saveSpecialty(Long id, String code, String name, int durationMinutes, boolean active);
    SpecialtyItem changeSpecialtyActive(long id, boolean active);
    CatalogItem changeEpsActive(long id, boolean active);
    PlanItem changePlanActive(long id, boolean active);
    Optional<Affiliation> affiliation(UUID userId);
    Affiliation saveAffiliation(UUID userId, long epsId, long planId, String regimeCode);
    record CatalogItem(long id, String code, String name, boolean active) { }
    record PlanItem(long id, long epsId, String code, String name, boolean active) { }
    record SpecialtyItem(long id, String code, String name, int durationMinutes, boolean active) { }
    record Affiliation(long epsId, String epsName, long planId, String planName, String regimeCode, String regimeName) { }
}
