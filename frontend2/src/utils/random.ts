import CustomRandom from "./CustomRandom";

/**
 * получить случайное число в диапазоне (0..max-1)
 */
export function getRandomInt(max: number): number {
    return CustomRandom.instance.nextInt() % max
}

/**
 * детерминированный рандом на основании координат
 */
export function getRandomByCoord(x: number, y: number, z?: number, s?: number): number {
    let seed = s !== undefined ? s : x
    seed = (seed * 1103515245 + 12345) % 2147483647;
    seed *= y + x
    seed = (seed * 1103515245 + 12345) % 2147483647;
    seed = (seed * 1103515245 + 12345) % 2147483647;

    if (z != undefined) {
        seed *= z
        seed = (seed * 1103515245 + 12345) % 2147483647;
        seed = (seed * 1103515245 + 12345) % 2147483647;
    }

    return seed
}
