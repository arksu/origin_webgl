export const padMixin = {
    methods: {
        /**
         * leading zero for numbers
         */
        pad(num: number, size: number) {
            let n = num.toString();
            while (n.length < size) n = "0" + num;
            return n;
        }
    }
}