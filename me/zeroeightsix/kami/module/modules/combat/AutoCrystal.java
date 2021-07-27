package me.zeroeightsix.kami.module.modules.combat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.event.events.PacketEvent.Send;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.KamiTessellator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

@Module$Info(
   name = "AutoCrystal",
   category = Module$Category.COMBAT
)
public class AutoCrystal extends Module {
   private Setting<Boolean> autoSwitch = this.register(Settings.b("Auto Switch"));
   private Setting<Boolean> players = this.register(Settings.b("Players"));
   private Setting<Boolean> mobs = this.register(Settings.b("Mobs", false));
   private Setting<Boolean> animals = this.register(Settings.b("Animals", false));
   private Setting<Boolean> place = this.register(Settings.b("Place", false));
   private Setting<Boolean> slow = this.register(Settings.b("Slow", false));
   private Setting<Double> BreakRange = this.register(Settings.d("Break Range", 6.0D));
   private Setting<Double> PlaceRange = this.register(Settings.d("Place Range", 6.0D));
   private Setting<Boolean> antiWeakness = this.register(Settings.b("Anti Weakness", false));
   private Setting<Boolean> Walls = this.register(Settings.b("Walls", false));
   private Setting<Double> WallsRange = this.register(Settings.d("Walls Range", 4.5D));
   private Setting<Boolean> rotate = this.register(Settings.b("Rotate", true));
   private Setting<Double> placereset = this.register(Settings.d("Placements before rotate", 1.0D));
   private Setting<Double> breakreset = this.register(Settings.d("Breaks before reset", 1.0D));
   private Setting<Boolean> delay = this.register(Settings.b("Delay", true));
   private Setting<Boolean> suicide = this.register(Settings.b("Suicide Protect", true));
   private Setting<Boolean> RayTrace = this.register(Settings.b("RayTrace", true));
   private Setting<Boolean> Return = this.register(Settings.b("Return", true));
   private BlockPos render;
   private Entity renderEnt;
   private long systemTime = -1L;
   private static boolean togglePitch = false;
   private boolean switchCooldown = false;
   private boolean isAttacking = false;
   private int oldSlot = -1;
   private int newSlot;
   double playerDistance;
   private int breaks;
   private int placements;
   private boolean uniDamage;
   private static boolean isSpoofingAngles;
   private static double yaw;
   private static double pitch;
   @EventHandler
   private Listener<Send> packetListener = new Listener((event) -> {
      Packet packet = event.getPacket();
      if (packet instanceof CPacketPlayer && isSpoofingAngles) {
         ((CPacketPlayer)packet).field_149476_e = (float)yaw;
         ((CPacketPlayer)packet).field_149473_f = (float)pitch;
      }

   }, new Predicate[0]);

   public void onUpdate() {
      double Placements = 0.0D;
      EntityEnderCrystal crystal = (EntityEnderCrystal)mc.field_71441_e.field_72996_f.stream().filter((entityx) -> {
         return entityx instanceof EntityEnderCrystal;
      }).map((entityx) -> {
         return (EntityEnderCrystal)entityx;
      }).min(Comparator.comparing((c) -> {
         return mc.field_71439_g.func_70032_d(c);
      })).orElse((Object)null);
      if (crystal != null && (double)mc.field_71439_g.func_70032_d(crystal) <= (Double)this.BreakRange.getValue()) {
         if ((Boolean)this.Walls.getValue() && !mc.field_71439_g.func_70685_l(crystal) && (double)mc.field_71439_g.func_70032_d(crystal) <= (Double)this.WallsRange.getValue()) {
            this.lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, mc.field_71439_g);
            mc.field_71442_b.func_78764_a(mc.field_71439_g, crystal);
            mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
            if ((Boolean)this.delay.getValue()) {
               this.systemTime = System.nanoTime() / 9000000L;
            }

            ++this.breaks;
         } else if ((Boolean)this.Walls.getValue() && mc.field_71439_g.func_70685_l(crystal)) {
            this.lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, mc.field_71439_g);
            mc.field_71442_b.func_78764_a(mc.field_71439_g, crystal);
            mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
            if ((Boolean)this.delay.getValue()) {
               this.systemTime = System.nanoTime() / 90000000L;
            }

            ++this.breaks;
         } else if (!(Boolean)this.Walls.getValue()) {
            this.lookAtPacket(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, mc.field_71439_g);
            mc.field_71442_b.func_78764_a(mc.field_71439_g, crystal);
            mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
            if ((Boolean)this.delay.getValue()) {
               this.systemTime = System.nanoTime() / 90000000L;
            }

            ++this.breaks;
         }

         if ((double)this.breaks == (Double)this.breakreset.getValue()) {
            if ((Boolean)this.rotate.getValue()) {
               resetRotation();
            }

            this.breaks = 0;
            if ((Boolean)this.Return.getValue()) {
               return;
            }
         }
      } else {
         if ((Boolean)this.rotate.getValue()) {
            resetRotation();
         }

         if (this.oldSlot != -1) {
            Wrapper.getPlayer().field_71071_by.field_70461_c = this.oldSlot;
            this.oldSlot = -1;
         }

         this.isAttacking = false;
      }

      int crystalSlot = mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP ? mc.field_71439_g.field_71071_by.field_70461_c : -1;
      if (crystalSlot == -1) {
         for(int l = 0; l < 9; ++l) {
            if (mc.field_71439_g.field_71071_by.func_70301_a(l).func_77973_b() == Items.field_185158_cP) {
               crystalSlot = l;
               break;
            }
         }
      }

      boolean offhand = false;
      if (mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP) {
         offhand = true;
      } else if (crystalSlot == -1) {
         return;
      }

      List<BlockPos> blocks = this.findCrystalBlocks();
      List<Entity> entities = new ArrayList();
      if ((Boolean)this.players.getValue()) {
         entities.addAll((Collection)mc.field_71441_e.field_73010_i.stream().filter((entityPlayer) -> {
            return !Friends.isFriend(entityPlayer.func_70005_c_());
         }).collect(Collectors.toList()));
      }

      entities.addAll((Collection)mc.field_71441_e.field_72996_f.stream().filter((entityx) -> {
         return EntityUtil.isLiving(entityx) && EntityUtil.isPassive(entityx) ? (Boolean)this.animals.getValue() : (Boolean)this.mobs.getValue();
      }).collect(Collectors.toList()));
      BlockPos q = null;
      double damage = 0.5D;
      Iterator var11 = entities.iterator();

      label185:
      while(true) {
         Entity entity;
         do {
            do {
               if (!var11.hasNext()) {
                  if (damage == 0.5D) {
                     this.render = null;
                     this.renderEnt = null;
                     if ((Boolean)this.rotate.getValue()) {
                        resetRotation();
                     }

                     return;
                  }

                  this.render = q;
                  if ((Boolean)this.place.getValue()) {
                     if (!offhand && mc.field_71439_g.field_71071_by.field_70461_c != crystalSlot) {
                        if ((Boolean)this.autoSwitch.getValue()) {
                           mc.field_71439_g.field_71071_by.field_70461_c = crystalSlot;
                           if ((Boolean)this.rotate.getValue()) {
                              resetRotation();
                           }

                           this.switchCooldown = true;
                        }

                        return;
                     }

                     this.lookAtPacket((double)q.field_177962_a + 0.5D, (double)q.field_177960_b - 0.5D, (double)q.field_177961_c + 0.5D, mc.field_71439_g);
                     EnumFacing f;
                     if (!(Boolean)this.RayTrace.getValue()) {
                        f = EnumFacing.UP;
                     } else {
                        RayTraceResult result = mc.field_71441_e.func_72933_a(new Vec3d(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + (double)mc.field_71439_g.func_70047_e(), mc.field_71439_g.field_70161_v), new Vec3d((double)q.field_177962_a + 0.5D, (double)q.field_177960_b - 0.5D, (double)q.field_177961_c + 0.5D));
                        if (result != null && result.field_178784_b != null) {
                           f = result.field_178784_b;
                        } else {
                           f = EnumFacing.UP;
                        }

                        if (this.switchCooldown) {
                           this.switchCooldown = false;
                           return;
                        }
                     }

                     mc.field_71439_g.field_71174_a.func_147297_a(new CPacketPlayerTryUseItemOnBlock(q, f, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                     ++this.placements;
                     if ((double)this.placements == (Double)this.placereset.getValue()) {
                        resetRotation();
                     }

                     if (isSpoofingAngles) {
                        EntityPlayerSP var10000;
                        if (togglePitch) {
                           var10000 = mc.field_71439_g;
                           var10000.field_70125_A = (float)((double)var10000.field_70125_A + 4.0E-4D);
                           togglePitch = false;
                        } else {
                           var10000 = mc.field_71439_g;
                           var10000.field_70125_A = (float)((double)var10000.field_70125_A - 4.0E-4D);
                           togglePitch = true;
                        }
                     }
                  }

                  return;
               }

               entity = (Entity)var11.next();
            } while(entity == mc.field_71439_g);
         } while(((EntityLivingBase)entity).func_110143_aJ() <= 0.0F);

         Iterator var13 = blocks.iterator();

         while(true) {
            BlockPos blockPos;
            double d;
            double self;
            do {
               do {
                  do {
                     double b;
                     do {
                        if (!var13.hasNext()) {
                           continue label185;
                        }

                        blockPos = (BlockPos)var13.next();
                        b = entity.func_174818_b(blockPos);
                        this.playerDistance = b;
                     } while(b >= 169.0D);

                     d = (double)calculateDamage((double)blockPos.field_177962_a + 0.5D, (double)(blockPos.field_177960_b + 1), (double)blockPos.field_177961_c + 0.5D, entity);
                     if (d >= 12.0D) {
                        this.uniDamage = true;
                     } else {
                        this.uniDamage = false;
                     }
                  } while(!(d > damage));

                  self = (double)calculateDamage((double)blockPos.field_177962_a + 0.5D, (double)(blockPos.field_177960_b + 1), (double)blockPos.field_177961_c + 0.5D, mc.field_71439_g);
               } while(self > d && !(d < (double)((EntityLivingBase)entity).func_110143_aJ()));
            } while(self - 0.5D > (double)mc.field_71439_g.func_110143_aJ() && (Boolean)this.suicide.getValue());

            damage = d;
            q = blockPos;
            this.renderEnt = entity;
         }
      }
   }

   public void onWorldRender(RenderEvent event) {
      if (this.render != null) {
         Vec3d var2;
         if (this.uniDamage) {
            KamiTessellator.prepare(7);
            KamiTessellator.drawBox(this.render, -1718026087, 63);
            KamiTessellator.release();
            if (this.renderEnt != null) {
               var2 = EntityUtil.getInterpolatedRenderPos(this.renderEnt, mc.func_184121_ak());
            }
         } else {
            KamiTessellator.prepare(7);
            KamiTessellator.drawBox(this.render, 1352204441, 63);
            KamiTessellator.release();
            if (this.renderEnt != null) {
               var2 = EntityUtil.getInterpolatedRenderPos(this.renderEnt, mc.func_184121_ak());
            }
         }
      }

   }

   private void lookAtPacket(double px, double py, double pz, EntityPlayer me) {
      double[] v = EntityUtil.calculateLookAt(px, py, pz, me);
      setYawAndPitch((float)v[0], (float)v[1]);
   }

   private boolean canPlaceCrystal(BlockPos blockPos) {
      BlockPos boost = blockPos.func_177982_a(0, 1, 0);
      BlockPos boost2 = blockPos.func_177982_a(0, 2, 0);
      return (mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150357_h || mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150343_Z) && mc.field_71441_e.func_180495_p(boost).func_177230_c() == Blocks.field_150350_a && mc.field_71441_e.func_180495_p(boost2).func_177230_c() == Blocks.field_150350_a && mc.field_71441_e.func_72872_a(Entity.class, new AxisAlignedBB(boost)).isEmpty();
   }

   public static BlockPos getPlayerPos() {
      return new BlockPos(Math.floor(mc.field_71439_g.field_70165_t), Math.floor(mc.field_71439_g.field_70163_u), Math.floor(mc.field_71439_g.field_70161_v));
   }

   private List<BlockPos> findCrystalBlocks() {
      NonNullList<BlockPos> positions = NonNullList.func_191196_a();
      positions.addAll((Collection)this.getSphere(getPlayerPos(), ((Double)this.PlaceRange.getValue()).floatValue(), ((Double)this.PlaceRange.getValue()).intValue(), false, true, 0).stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
      return positions;
   }

   public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
      List<BlockPos> circleblocks = new ArrayList();
      int cx = loc.func_177958_n();
      int cy = loc.func_177956_o();
      int cz = loc.func_177952_p();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            for(int y = sphere ? cy - (int)r : cy; (float)y < (sphere ? (float)cy + r : (float)(cy + h)); ++y) {
               double dist = (double)((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0));
               if (dist < (double)(r * r) && (!hollow || !(dist < (double)((r - 1.0F) * (r - 1.0F))))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleblocks.add(l);
               }
            }
         }
      }

      return circleblocks;
   }

   public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
      float doubleExplosionSize = 12.0F;
      double distancedsize = entity.func_70011_f(posX, posY, posZ) / (double)doubleExplosionSize;
      Vec3d vec3d = new Vec3d(posX, posY, posZ);
      double blockDensity = (double)entity.field_70170_p.func_72842_a(vec3d, entity.func_174813_aQ());
      double v = (1.0D - distancedsize) * blockDensity;
      float damage = (float)((int)((v * v + v) / 2.0D * 12.0D * (double)doubleExplosionSize + 1.0D));
      double finald = 1.0D;
      if (entity instanceof EntityLivingBase) {
         finald = (double)getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion(mc.field_71441_e, (Entity)null, posX, posY, posZ, 6.0F, false, true));
      }

      return (float)finald;
   }

   public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer ep = (EntityPlayer)entity;
         DamageSource ds = DamageSource.func_94539_a(explosion);
         damage = CombatRules.func_189427_a(damage, (float)ep.func_70658_aO(), (float)ep.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
         int k = EnchantmentHelper.func_77508_a(ep.func_184193_aE(), ds);
         float f = MathHelper.func_76131_a((float)k, 0.0F, 20.0F);
         damage *= 1.0F - f / 25.0F;
         if (entity.func_70644_a(Potion.func_188412_a(11))) {
            damage -= damage / 4.0F;
         }

         damage = Math.max(damage - ep.func_110139_bj(), 0.0F);
         return damage;
      } else {
         damage = CombatRules.func_189427_a(damage, (float)entity.func_70658_aO(), (float)entity.func_110148_a(SharedMonsterAttributes.field_189429_h).func_111126_e());
         return damage;
      }
   }

   private static float getDamageMultiplied(float damage) {
      int diff = mc.field_71441_e.func_175659_aa().func_151525_a();
      return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
   }

   public static float calculateDamage(EntityEnderCrystal crystal, Entity entity) {
      return calculateDamage(crystal.field_70165_t, crystal.field_70163_u, crystal.field_70161_v, entity);
   }

   private static void setYawAndPitch(float yaw1, float pitch1) {
      yaw = (double)yaw1;
      pitch = (double)pitch1;
      isSpoofingAngles = true;
   }

   private static void resetRotation() {
      if (isSpoofingAngles) {
         yaw = (double)mc.field_71439_g.field_70177_z;
         pitch = (double)mc.field_71439_g.field_70125_A;
         isSpoofingAngles = false;
      }

   }

   public void onDisable() {
      this.render = null;
      this.renderEnt = null;
      if ((Boolean)this.rotate.getValue()) {
         resetRotation();
      }

   }
}
