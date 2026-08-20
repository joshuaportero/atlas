package dev.portero.atlas.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatListener implements Listener {

    private final CombatManager combat;
    private final Map<EntityDamageByEntityEvent, DamageContext> pending = new ConcurrentHashMap<>();

    public CombatListener(CombatManager combat) {
        this.combat = combat;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!this.combat.enabled() || this.combat.applying()
                || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        Entity attacker = this.resolveAttacker(event.getDamager());
        DamageContext context = this.combat.calculate(
                attacker, event.getEntity(), event.getDamage(), DamageType.PHYSICAL);

        if (context.cancelled()) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(context.currentDamage());
        this.pending.put(event, context);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageMonitor(EntityDamageByEntityEvent event) {
        DamageContext context = this.pending.remove(event);
        if (context == null || event.isCancelled()) {
            return;
        }
        this.combat.finish(context);
    }

    private Entity resolveAttacker(Entity damager) {
        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Entity shooter) {
            return shooter;
        }
        return damager;
    }
}
