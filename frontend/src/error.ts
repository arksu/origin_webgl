import {showLoginPage} from "./login";
import {hideCharacters} from "./characters";

export function showError(msg: string) {
    hideCharacters();

    document.getElementById("error-page").style.display = "block";

    document.getElementById("error-text").innerHTML = msg;

    (<HTMLButtonElement>document.getElementById("error-btn")).onclick = function (e) {
        e.preventDefault();
        document.getElementById("error-page").style.display = "none";
        showLoginPage();
    }
}