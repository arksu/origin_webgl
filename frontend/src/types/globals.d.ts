import Client from "../net/Client";

declare global {
    // interface Window {
    //     _: any;
    // }

    function setTimeout(callback: (...args: any[]) => void, ms: number, ...args: any[]): number;

    type Callback = () => void;
}
export {};