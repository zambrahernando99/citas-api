package co.academy.citas.adapter.in.web;

import co.academy.citas.application.port.out.CatalogPort;
import co.academy.citas.application.service.CatalogManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogManagementService catalogs;
    public CatalogController(CatalogManagementService catalogs) { this.catalogs = catalogs; }

    @GetMapping("/eps") List<CatalogResponse> activeEps() { return catalogs.eps(true).stream().map(CatalogResponse::from).toList(); }
    @GetMapping("/eps/{epsId}/plans") List<PlanResponse> activePlans(@PathVariable long epsId) { return catalogs.plans(epsId,true).stream().map(PlanResponse::from).toList(); }
    @GetMapping("/regimes") List<CatalogResponse> regimes() { return catalogs.regimes(true).stream().map(CatalogResponse::from).toList(); }
    @GetMapping("/profile/affiliation") AffiliationResponse affiliation(Principal principal) { return AffiliationResponse.from(catalogs.affiliation(actor(principal))); }
    @PutMapping("/profile/affiliation") AffiliationResponse saveAffiliation(Principal principal,@Valid @RequestBody AffiliationRequest request) {
        return AffiliationResponse.from(catalogs.saveAffiliation(actor(principal),request.epsId(),request.planId(),request.regimeCode()));
    }

    @GetMapping("/admin/eps") List<CatalogResponse> allEps() { return catalogs.eps(false).stream().map(CatalogResponse::from).toList(); }
    @PostMapping("/admin/eps") ResponseEntity<CatalogResponse> createEps(@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CatalogResponse.from(catalogs.saveEps(null,request.code(),request.name(),true)));
    }
    @PutMapping("/admin/eps/{id}") CatalogResponse updateEps(@PathVariable long id,@Valid @RequestBody CatalogRequest request) {
        boolean active=catalogs.eps(false).stream().filter(item->item.id()==id).findFirst().orElseThrow().active();
        return CatalogResponse.from(catalogs.saveEps(id,request.code(),request.name(),active));
    }
    @PatchMapping("/admin/eps/{id}/active") CatalogResponse setEpsActive(@PathVariable long id,@RequestBody ActiveRequest request) { return CatalogResponse.from(catalogs.setEpsActive(id,request.active())); }
    @DeleteMapping("/admin/eps/{id}") ResponseEntity<Void> deactivateEps(@PathVariable long id) { catalogs.setEpsActive(id,false); return ResponseEntity.noContent().build(); }
    @GetMapping("/admin/eps/{epsId}/plans") List<PlanResponse> allPlans(@PathVariable long epsId) { return catalogs.plans(epsId,false).stream().map(PlanResponse::from).toList(); }
    @PostMapping("/admin/eps/{epsId}/plans") ResponseEntity<PlanResponse> createPlan(@PathVariable long epsId,@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PlanResponse.from(catalogs.savePlan(null,epsId,request.code(),request.name(),true)));
    }
    @PutMapping("/admin/plans/{id}") PlanResponse updatePlan(@PathVariable long id,@Valid @RequestBody PlanRequest request) {
        PlanResponse current=catalogs.eps(false).stream().flatMap(e->catalogs.plans(e.id(),false).stream()).filter(p->p.id()==id).findFirst().map(PlanResponse::from).orElseThrow();
        return PlanResponse.from(catalogs.savePlan(id,request.epsId(),request.code(),request.name(),current.active()));
    }
    @PatchMapping("/admin/plans/{id}/active") PlanResponse setPlanActive(@PathVariable long id,@RequestBody ActiveRequest request) { return PlanResponse.from(catalogs.setPlanActive(id,request.active())); }
    @DeleteMapping("/admin/plans/{id}") ResponseEntity<Void> deactivatePlan(@PathVariable long id) { catalogs.setPlanActive(id,false); return ResponseEntity.noContent().build(); }

    @GetMapping("/admin/specialties") List<SpecialtyResponse> allSpecialties() { return catalogs.specialties(false).stream().map(SpecialtyResponse::from).toList(); }
    @PostMapping("/admin/specialties") ResponseEntity<SpecialtyResponse> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SpecialtyResponse.from(catalogs.saveSpecialty(null,request.code(),request.name(),request.durationMinutes(),true)));
    }
    @PutMapping("/admin/specialties/{id}") SpecialtyResponse updateSpecialty(@PathVariable long id,@Valid @RequestBody SpecialtyRequest request) {
        boolean active=catalogs.specialties(false).stream().filter(item->item.id()==id).findFirst().orElseThrow().active();
        return SpecialtyResponse.from(catalogs.saveSpecialty(id,request.code(),request.name(),request.durationMinutes(),active));
    }
    @PatchMapping("/admin/specialties/{id}/active") SpecialtyResponse setSpecialtyActive(@PathVariable long id,@RequestBody ActiveRequest request) { return SpecialtyResponse.from(catalogs.setSpecialtyActive(id,request.active())); }
    @DeleteMapping("/admin/specialties/{id}") ResponseEntity<Void> deactivateSpecialty(@PathVariable long id) { catalogs.setSpecialtyActive(id,false); return ResponseEntity.noContent().build(); }

    @GetMapping("/admin/regimes") List<CatalogResponse> allRegimes() { return catalogs.regimes(false).stream().map(CatalogResponse::from).toList(); }
    @PostMapping("/admin/regimes") ResponseEntity<CatalogResponse> createRegime(@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CatalogResponse.from(catalogs.saveRegime(request.code(),request.name(),true)));
    }
    @PutMapping("/admin/regimes/{code}") CatalogResponse updateRegime(@PathVariable String code,@Valid @RequestBody CatalogRequest request) {
        boolean active=catalogs.regimes(false).stream().filter(item->item.code().equals(code)).findFirst().orElseThrow().active();
        return CatalogResponse.from(catalogs.saveRegime(code,request.name(),active));
    }
    @PatchMapping("/admin/regimes/{code}/active") CatalogResponse setRegimeActive(@PathVariable String code,@RequestBody ActiveRequest request) { return CatalogResponse.from(catalogs.setRegimeActive(code,request.active())); }
    @DeleteMapping("/admin/regimes/{code}") ResponseEntity<Void> deactivateRegime(@PathVariable String code) { catalogs.setRegimeActive(code,false); return ResponseEntity.noContent().build(); }

    private UUID actor(Principal principal) { return UUID.fromString(principal.getName()); }
    record CatalogRequest(@NotBlank String code,@NotBlank String name) { }
    record PlanRequest(@Positive long epsId,@NotBlank String code,@NotBlank String name) { }
    record SpecialtyRequest(@NotBlank String code,@NotBlank String name,@NotNull @Positive Integer durationMinutes) { }
    record AffiliationRequest(@Positive long epsId,@Positive long planId,@NotBlank String regimeCode) { }
    record ActiveRequest(boolean active) { }
    record CatalogResponse(long id,String code,String name,boolean active) { static CatalogResponse from(CatalogPort.CatalogItem v){return new CatalogResponse(v.id(),v.code(),v.name(),v.active());} }
    record PlanResponse(long id,long epsId,String code,String name,boolean active) { static PlanResponse from(CatalogPort.PlanItem v){return new PlanResponse(v.id(),v.epsId(),v.code(),v.name(),v.active());} }
    record SpecialtyResponse(long id,String code,String name,int durationMinutes,boolean active) { static SpecialtyResponse from(CatalogPort.SpecialtyItem v){return new SpecialtyResponse(v.id(),v.code(),v.name(),v.durationMinutes(),v.active());} }
    record AffiliationResponse(long epsId,String epsName,long planId,String planName,String regimeCode,String regimeName) {
        static AffiliationResponse from(CatalogPort.Affiliation value){return value==null?null:new AffiliationResponse(value.epsId(),value.epsName(),value.planId(),value.planName(),value.regimeCode(),value.regimeName());}
    }
}
