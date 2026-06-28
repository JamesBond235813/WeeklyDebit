<script setup lang="ts">
import type { MenuRecordBadgeRaw } from '@vben-core/typings';

import { computed } from 'vue';

import { isValidColor } from '@vben-core/shared/color';

import BadgeDot from './menu-badge-dot.vue';

interface Props extends MenuRecordBadgeRaw {
  hasChildren?: boolean;
}

const props = withDefaults(defineProps<Props>(), {});

const variantsMap: Record<string, string> = {
  default: 'bg-green-500',
  destructive: 'bg-destructive',
  primary: 'bg-primary',
  success: 'bg-green-500',
  warning: 'bg-yellow-500',
};

const isDot = computed(() => props.badgeType === 'dot');
const isOutlineSuccess = computed(() => props.badgeVariants === 'outline-success');

const badgeClass = computed(() => {
  const { badgeVariants } = props;

  if (isOutlineSuccess.value) {
    return '';
  }

  if (!badgeVariants) {
    return variantsMap.default;
  }

  return variantsMap[badgeVariants] || badgeVariants;
});

const badgeStyle = computed(() => {
  if (isOutlineSuccess.value) {
    return {
      backgroundColor: 'transparent',
      border: '2px solid #16a34a',
      color: '#16a34a',
    };
  }
  if (badgeClass.value && isValidColor(badgeClass.value)) {
    return {
      backgroundColor: badgeClass.value,
    };
  }
  return {};
});
</script>
<template>
  <span v-if="isDot || badge" :class="$attrs.class" class="absolute">
    <BadgeDot v-if="isDot" :dot-class="badgeClass" :dot-style="badgeStyle" />
    <div
      v-else
      :class="badgeClass"
      :style="badgeStyle"
      class="text-primary-foreground flex-center rounded-xl px-1.5 py-0.5 text-[10px]"
      :data-badge-variant="badgeVariants"
    >
      {{ badge }}
    </div>
  </span>
</template>

<style scoped>
[data-badge-variant='outline-success'] {
  min-width: 18px !important;
  height: 18px !important;
  padding: 0 5px !important;
  color: #16a34a !important;
  background: #f0fdf4 !important;
  border: 1.5px solid #22c55e !important;
  border-radius: 999px;
  font-size: 11px !important;
  font-weight: 700 !important;
  line-height: 16px !important;
  box-shadow: none;
}
</style>
