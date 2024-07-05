/**
 * Linear congruential generator
 */
export default class CustomRandom {
    public static readonly instance: CustomRandom = new CustomRandom()

    private _seed: number

    constructor(s?: number) {
        if (s !== undefined) {
            this._seed = s
        } else {
            this._seed = Date.now();
        }
    }

    public nextInt(): number {
        this._seed = (this._seed * 1103515245 + 12345) % 2147483647;
        return this._seed
    }

    public nextDouble(): number {
        return this.nextInt() / 2147483647
    }

}
