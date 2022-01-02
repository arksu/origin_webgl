export default interface Coord extends Array<number | string> {
    0: number;
    1: number;
    length: 2; // это литеральный тип '2', это не значение!
}
