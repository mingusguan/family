interface RgbColor {
  red: number;
  green: number;
  blue: number;
}

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

export function getTagColorStyle(color?: string, active = false) {
  const rgb = color ? parseTagColor(color) : undefined;
  if (!rgb) return {};

  const backgroundAlpha = active ? 0.2 : 0.1;
  const borderAlpha = active ? 0.48 : 0.24;

  return {
    color: `rgb(${rgb.red}, ${rgb.green}, ${rgb.blue})`,
    backgroundColor: `rgba(${rgb.red}, ${rgb.green}, ${rgb.blue}, ${backgroundAlpha})`,
    borderColor: `rgba(${rgb.red}, ${rgb.green}, ${rgb.blue}, ${borderAlpha})`,
  };
}
