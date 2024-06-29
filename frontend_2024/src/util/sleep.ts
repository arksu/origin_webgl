export default async function sleep(ms: number) {
    return new Promise(r => setTimeout(r, ms));
}
