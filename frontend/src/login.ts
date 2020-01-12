import Net from "./net/Net";
import Client from "./net/Client";
import {showCharacters} from "./characters";

let errorMessageTimer;

function log2(n) {
    let log = 0;
    if ((n & 0xffff0000) !== 0) {
        n >>>= 16;
        log = 16;
    }
    if (n >= 256) {
        n >>>= 8;
        log += 8;
    }
    if (n >= 16) {
        n >>>= 4;
        log += 4;
    }
    if (n >= 4) {
        n >>>= 2;
        log += 2;
    }
    log = log + (n >>> 1);
    return log;
}

function hexToBase64(str) {
    return btoa(String.fromCharCode.apply(null, str.replace(/\r|\n/g, '')
        .replace(/([\da-fA-F]{2}) ?/g, '0x$1 ')
        .replace(RegExp(' +$'), '')
        .split(' ')));
}

export function setLoginForm() {
    let loginForm = document.getElementById("login-form");
    let registerForm = document.getElementById("register-form");

    loginForm.onsubmit = function (e) {
        e.preventDefault();
        console.log("login!");

        clearLoginError();

        let login = (<HTMLInputElement>document.getElementById("input-login")).value;
        let password = (<HTMLInputElement>document.getElementById("input-password")).value;
        let loginBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));

        loginBtn.disabled = true;
        doLogin(login, password);
    };

    registerForm.onsubmit = function (e) {
        e.preventDefault();

        console.log("register");

        let login = (<HTMLInputElement>document.getElementById("reg-login")).value;
        let password = (<HTMLInputElement>document.getElementById("reg-password")).value;
        let email = (<HTMLInputElement>document.getElementById("reg-email")).value;
        let btn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("reg-btn"));

        clearLoginError();

        if (!login || !password) {
            showLoginError("Username or password is empty");
        } else {
            btn.disabled = true;
            Net.instance.remoteCall("register", {
                login,
                password,
                email: email
            })
                .then(d => {
                    btn.disabled = false;
                    successLogin(login, password, d);
                })
                .catch(e => {
                    console.error(e);
                    if (e === "username busy") {
                        showLoginError("This username is taken");
                    }
                    btn.disabled = false;
                });
        }
    };

    document.getElementById("register-btn").onclick = function (e) {
        e.preventDefault();
        clearLoginError();

        loginForm.style.display = "none";
        registerForm.style.display = "block";
    };
    document.getElementById("sign-btn").onclick = function (e) {
        e.preventDefault();
        clearLoginError();

        loginForm.style.display = "block";
        registerForm.style.display = "none";
    };
}

function clearLoginError() {
    clearTimeout(errorMessageTimer);
    const error = document.getElementById("login-error");
    error.style.display = "none";
}

function showLoginError(msg: string): any {
    document.getElementById("login-page").style.display = "block";

    const error = document.getElementById("login-error");

    error.innerHTML = msg;
    error.style.display = "block";
    errorMessageTimer = setTimeout(() => {
        error.style.display = "none";
    }, 3000);
}

export function showLoginPage() {
    document.getElementById("login-page").style.display = "block";
    document.getElementById("login-form").style.display = "block";
    document.getElementById("register-form").style.display = "none";
    (<HTMLButtonElement>document.getElementById("login-btn")).disabled = false;
}

export function doLogin(login: string, password: string) {
    const scrypt = require('scryptsy');

    const N = 2048, r = 8, p = 1;
    const dkLen = 32;

    let bs = new Buffer(16);
    window.crypto.getRandomValues(bs);
    let saltHex = bs.toString('hex');

    let hashHex = scrypt(password, bs, N, r, p, dkLen).toString('hex');

    let params: any = log2(N) << 16 | r << 8 | p;
    params = params.toString(16);
    let hash = '$s0$' + params + '$' + hexToBase64(saltHex) + '$' + hexToBase64(hashHex);

    Net.instance.remoteCall("login", {
        login,
        password: hash
    })
        .then((d) => {
            successLogin(login, password, d);
        })
        .catch((e) => {
            console.error(e);

            let loginBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));
            loginBtn.disabled = false;

            showLoginError(e);
        });
}

function successLogin(login: string, password: string, d: any) {
    localStorage.setItem("login", login);
    localStorage.setItem("password", password);

    let loginPage = document.getElementById("login-page");
    loginPage.style.display = "none";

    Client.instance = new Client();
    Client.instance.ssid = d.ssid;

    Net.instance.gameCall("getCharacters")
        .then((d) => {
            showCharacters(d);
        });
}