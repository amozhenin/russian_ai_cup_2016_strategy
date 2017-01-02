import model.SkillType;

import java.util.List;

/**
 * Created by dvorkin on 02.01.2017.
 */
public class RangeStrategy extends MyStrategy {

    @Override
    protected List<SkillType> getDesiredSkills() {
        List<SkillType> skills = getDataStorage().getDesiredSkills();

        skills.add(SkillType.RANGE_BONUS_PASSIVE_1);
        skills.add(SkillType.RANGE_BONUS_AURA_1);
        skills.add(SkillType.RANGE_BONUS_PASSIVE_2);
        skills.add(SkillType.RANGE_BONUS_AURA_2);
        skills.add(SkillType.ADVANCED_MAGIC_MISSILE);

        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_1);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_AURA_1);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_2);
        skills.add(SkillType.MAGICAL_DAMAGE_BONUS_AURA_2);
        skills.add(SkillType.FROST_BOLT);

        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_1);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_1);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_2);
        skills.add(SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_2);

        skills.add(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_1);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_AURA_1);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_2);
        skills.add(SkillType.STAFF_DAMAGE_BONUS_AURA_2);

        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_1);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_AURA_1);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_2);
        skills.add(SkillType.MOVEMENT_BONUS_FACTOR_AURA_2);
        skills.add(SkillType.SHIELD);
        skills.add(SkillType.HASTE);
        skills.add(SkillType.FIREBALL);
        return skills;
    }
}
