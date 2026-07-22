<script setup lang="ts">
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { useThemeStore } from "@/store";
import { PENDING_FAMILY_INVITE_KEY } from "@/constants";
import { Storage } from "@/utils/storage";

function captureFamilyInvite(options: any) {
  const inviteCode = options?.query?.inviteCode;
  if (inviteCode) Storage.set(PENDING_FAMILY_INVITE_KEY, decodeURIComponent(inviteCode));
}

onLaunch((options) => {
  const themeStore = useThemeStore();
  themeStore.initTheme();
  captureFamilyInvite(options);
});

onShow(captureFamilyInvite);
</script>

<style lang="scss">
@use "@wot-ui/ui/styles/theme/index.scss" as *;
</style>
