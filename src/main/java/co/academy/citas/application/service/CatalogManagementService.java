package co.academy.citas.application.service;

import co.academy.citas.application.port.out.CatalogPort;
import co.academy.citas.application.port.out.CatalogPort.Affiliation;
import co.academy.citas.application.port.out.CatalogPort.CatalogItem;
import co.academy.citas.application.port.out.CatalogPort.PlanItem;
import co.academy.citas.application.port.out.CatalogPort.SpecialtyItem;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogManagementService {
    private final CatalogPort catalogs;
    public CatalogManagementService(CatalogPort catalogs) { this.catalogs = catalogs; }
    @Transactional(readOnly=true) public List<CatalogItem> eps(boolean active) { return catalogs.eps(active); }
    public CatalogItem saveEps(Long id,String code,String name,boolean active) { return catalogs.saveEps(id,required(code),required(name),active); }
    public CatalogItem setEpsActive(long id,boolean active) { return catalogs.changeEpsActive(id,active); }
    @Transactional(readOnly=true) public List<PlanItem> plans(long epsId,boolean active) { return catalogs.plans(epsId,active); }
    public PlanItem savePlan(Long id,long epsId,String code,String name,boolean active) {
        if(catalogs.eps(true).stream().noneMatch(value->value.id()==epsId)) throw new IllegalArgumentException("EPS is unavailable");
        return catalogs.savePlan(id,epsId,required(code),required(name),active);
    }
    public PlanItem setPlanActive(long id,boolean active) { return catalogs.changePlanActive(id,active); }
    @Transactional(readOnly=true) public List<CatalogItem> regimes(boolean active) { return catalogs.regimes(active); }
    public CatalogItem saveRegime(String code,String name,boolean active) { return catalogs.saveRegime(required(code),required(name),active); }
    public CatalogItem setRegimeActive(String code,boolean active) { return catalogs.changeRegimeActive(code,active); }
    @Transactional(readOnly=true) public List<SpecialtyItem> specialties(boolean active) { return catalogs.specialties(active); }
    public SpecialtyItem saveSpecialty(Long id,String code,String name,int duration,boolean active) {
        if(duration!=30&&duration!=60) throw new IllegalArgumentException("Specialty duration must be 30 or 60 minutes");
        return catalogs.saveSpecialty(id,required(code),required(name),duration,active);
    }
    public SpecialtyItem setSpecialtyActive(long id,boolean active) { return catalogs.changeSpecialtyActive(id,active); }
    @Transactional(readOnly=true) public Affiliation affiliation(UUID userId) { return catalogs.affiliation(userId).orElse(null); }
    public Affiliation saveAffiliation(UUID userId,long epsId,long planId,String regimeCode) {
        if(catalogs.eps(true).stream().noneMatch(value->value.id()==epsId)) throw new IllegalArgumentException("EPS is inactive or missing");
        if(catalogs.plans(epsId,true).stream().noneMatch(value->value.id()==planId)) throw new IllegalArgumentException("Plan does not belong to the selected EPS or is inactive");
        if(catalogs.regimes(true).stream().noneMatch(value->value.code().equals(regimeCode))) throw new IllegalArgumentException("Regime is inactive or missing");
        return catalogs.saveAffiliation(userId,epsId,planId,regimeCode);
    }
    private String required(String value) { if(value==null||value.isBlank()) throw new IllegalArgumentException("Catalog fields are required"); return value.trim(); }
}
