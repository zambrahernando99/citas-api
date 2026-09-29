package co.academy.citas.adapter.out.persistence;

import co.academy.citas.application.port.out.CatalogPort;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CatalogPersistenceAdapter implements CatalogPort {
    private final JdbcTemplate jdbc;
    public CatalogPersistenceAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<CatalogItem> eps(boolean activeOnly) {
        return jdbc.query("select id,code,name,active from eps_catalog" + (activeOnly ? " where active = true" : "") + " order by name",
                (rs,row)->new CatalogItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getBoolean(4)));
    }
    @Override public CatalogItem saveEps(Long id,String code,String name,boolean active) {
        if(id==null) jdbc.update("insert into eps_catalog(code,name,active) values(?,?,?)",code,name,active);
        else jdbc.update("update eps_catalog set code=?,name=?,active=? where id=?",code,name,active,id);
        return jdbc.queryForObject("select id,code,name,active from eps_catalog where " + (id==null?"id=last_insert_id()":"id=?"),
                (rs,row)->new CatalogItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getBoolean(4)), id==null?new Object[]{}:new Object[]{id});
    }
    @Override public List<PlanItem> plans(long epsId,boolean activeOnly) {
        return jdbc.query("select id,eps_id,code,name,active from eps_plan where eps_id=?"+(activeOnly?" and active=true":"")+" order by name",
                (rs,row)->new PlanItem(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getBoolean(5)),epsId);
    }
    @Override public PlanItem savePlan(Long id,long epsId,String code,String name,boolean active) {
        if(id==null) jdbc.update("insert into eps_plan(eps_id,code,name,active) values(?,?,?,?)",epsId,code,name,active);
        else jdbc.update("update eps_plan set eps_id=?,code=?,name=?,active=? where id=?",epsId,code,name,active,id);
        return jdbc.queryForObject("select id,eps_id,code,name,active from eps_plan where "+(id==null?"id=last_insert_id()":"id=?"),
                (rs,row)->new PlanItem(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getBoolean(5)),id==null?new Object[]{}:new Object[]{id});
    }
    @Override public List<CatalogItem> regimes(boolean activeOnly) {
        return jdbc.query("select 0,code,name,active from regime_catalog"+(activeOnly?" where active=true":"")+" order by name",
                (rs,row)->new CatalogItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getBoolean(4)));
    }
    @Override public List<SpecialtyItem> specialties(boolean activeOnly) {
        return jdbc.query("select id,code,name,duration_minutes,active from specialty_catalog"+(activeOnly?" where active=true":"")+" order by name",
                (rs,row)->new SpecialtyItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getBoolean(5)));
    }
    @Override public SpecialtyItem saveSpecialty(Long id,String code,String name,int duration,boolean active) {
        if(id==null) jdbc.update("insert into specialty_catalog(code,name,duration_minutes,active) values(?,?,?,?)",code,name,duration,active);
        else jdbc.update("update specialty_catalog set code=?,name=?,duration_minutes=?,active=? where id=?",code,name,duration,active,id);
        return jdbc.queryForObject("select id,code,name,duration_minutes,active from specialty_catalog where "+(id==null?"id=last_insert_id()":"id=?"),
                (rs,row)->new SpecialtyItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getBoolean(5)),id==null?new Object[]{}:new Object[]{id});
    }
    @Override public SpecialtyItem changeSpecialtyActive(long id,boolean active) {
        jdbc.update("update specialty_catalog set active=? where id=?",active,id);
        return jdbc.queryForObject("select id,code,name,duration_minutes,active from specialty_catalog where id=?",
                (rs,row)->new SpecialtyItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getBoolean(5)),id);
    }
    @Override public CatalogItem changeEpsActive(long id,boolean active) {
        jdbc.update("update eps_catalog set active=? where id=?",active,id);
        return jdbc.queryForObject("select id,code,name,active from eps_catalog where id=?",
                (rs,row)->new CatalogItem(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getBoolean(4)),id);
    }
    @Override public PlanItem changePlanActive(long id,boolean active) {
        jdbc.update("update eps_plan set active=? where id=?",active,id);
        return jdbc.queryForObject("select id,eps_id,code,name,active from eps_plan where id=?",
                (rs,row)->new PlanItem(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getBoolean(5)),id);
    }
    @Override public Optional<Affiliation> affiliation(UUID userId) {
        return jdbc.query("select a.eps_id,e.name,a.plan_id,p.name,a.regime_code,r.name from user_affiliation a join eps_catalog e on e.id=a.eps_id join eps_plan p on p.id=a.plan_id join regime_catalog r on r.code=a.regime_code where a.user_id=?",
                (rs,row)->new Affiliation(rs.getLong(1),rs.getString(2),rs.getLong(3),rs.getString(4),rs.getString(5),rs.getString(6)),bytes(userId)).stream().findFirst();
    }
    @Override public Affiliation saveAffiliation(UUID userId,long epsId,long planId,String regimeCode) {
        jdbc.update("insert into user_affiliation(user_id,eps_id,plan_id,regime_code) values(?,?,?,?) on duplicate key update eps_id=values(eps_id),plan_id=values(plan_id),regime_code=values(regime_code),updated_at=current_timestamp(6)",bytes(userId),epsId,planId,regimeCode);
        return affiliation(userId).orElseThrow();
    }
    private static byte[] bytes(UUID value) { return ByteBuffer.allocate(16).putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()).array(); }
}
