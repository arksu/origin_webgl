import {showLoginPage} from "./login";
import {hideCharactersList} from "./characters";

export function showError(msg: string) {
    hideCharactersList();

    document.getElementById("error-page").style.display = "block";

    document.getElementById("error-text").innerHTML = msg;

    (<HTMLButtonElement>document.getElementById("error-btn")).onclick = function (e) {
        e.preventDefault();
        document.getElementById("error-page").style.display = "none";
        showLoginPage();
    }
}