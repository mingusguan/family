<template>
  <span class="album-tag-badge" :style="tagStyle">#{{ name }}</span>
</template>

<script setup lang="ts">
import type { CSSProperties } from "vue";

const props = defineProps<{
  name: string;
  color?: string;
}>();

interface RgbColor {
  red: number;
  green: number;
  blue: number;
}

const tagStyle = computed<CSSProperties>(() => {
  const rgb = props.color ? parseTagColor(props.color) : undefined;
  if (!rgb) return {};

  return {
    color: `rgb(${rgb.red}, ${rgb.green}, ${rgb.blue})`,
    backgroundColor: `rgba(${rgb.red}, ${rgb.green}, ${rgb.blue}, 0.1)`,
    borderColor: `rgba(${rgb.red}, ${rgb.green}, ${rgb.blue}, 0.24)`,
  };
});

function parseTagColor(color: string): RgbColor | undefined {
  const normalized = color.trim();
  const hexMatch = normalized.match(/^#([0-9a-f]{3}|[0-9a-f]{6})$/i);
  if (hexMatch) {
    const shorthand = hexMatch[1];
    const hex =
      shorthand.length === 3
        ? shorthand
            .split("")
            .map((item) => item + item)
            .join("")
        : shorthand;
    return {
      red: Number.parseInt(hex.slice(0, 2), 16),
      green: Number.parseInt(hex.slice(2, 4), 16),
      blue: Number.parseInt(hex.slice(4, 6), 16),
    };
  }

  const rgbMatch = normalized.match(
    /^rgba?\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})(?:\s*,\s*(?:0|1|0?\.\d+))?\s*\)$/i
  );
  if (!rgbMatch) return undefined;

  const channels = rgbMatch.slice(1, 4).map(Number);
  if (channels.some((channel) => channel < 0 || channel > 255)) return undefined;
  return { red: channels[0], green: channels[1], blue: channels[2] };
}
</script>

<style scoped>
.album-tag-badge {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 3px 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  line-height: 1.4;
  color: #6f60ce;
  white-space: nowrap;
  background: #efecff;
  border: 1px solid transparent;
  border-radius: 999px;
}
</style>
