# WhisperShadow 0.5.0

Client-only psychological horror mod for Minecraft Java 1.21.11 / Fabric.

## 0.5.0 additions

- Player-shaped Shadow/Doppelganger using the local player's GameProfile and skin instead of an Armor Stand.
- Client-only TAB-name horror messages based on names currently visible to the client.
- Rare eyes and peripheral silhouettes on the HUD.
- Unreliable UI/glitch overlays and world distortion effects.
- Rare local torch/lantern blackout within 20 blocks, restored automatically.
- Positional footsteps played behind the player with a subtitle entry.
- Delayed-motion Doppelganger that can watch or mirror the player.
- Existing whispers, chat replies, glitches, VHS effects, figure events and hidden insanity system remain.

Everything in this mod is intended to be client-side/local. The server is not sent a packet to create these horror entities or effects.

## Horror event test commands

All event effects can be tested locally with `/ws <name>`:

`message`, `glitch`, `whisper`, `figure`, `decoy`, `vhs`, `tab`, `eyes`, `footsteps`, `torches`, `doppel`, `peripheral`, `distort`, `run`, `broken`, `dontmove`, `findus`, `sign`, `silence`, `watcher`, `distortedfigure`, `wall`, `vhs_shift`, `blackout`, `nothing_shadow`, `ghost_block`, `fake_texture`, `mob_stare`, `cam_micro`, `cam_tilt`, `cam_look`, `cam_stick`, `cam_zoom`, `cam_far`, `cam_low`, `cam_high`, `cam_flip`, `cam_bar`, `cam_white`, `cam_red`, `cam_ghost`, `cam_double`, `cam_scan`, `hud_coords`, `hud_time`, `hud_hide`, `hud_double`, `hud_name`, `hud_flip`, `hud_symbol`, `hud_slot`, `hud_item`, `hud_health`, `hud_hearts`.
