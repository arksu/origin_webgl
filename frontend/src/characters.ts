import Net from "./net/Net";
import {showLoginPage} from "./login";
import Client from "./net/Client";
import Game from "./game/Game";

/**
 * показать список персонажей
 */
export function showCharactersList(list?: Array<any>) {
    document.getElementById("characters-page").style.display = "block";
    document.getElementById("characters-list").style.display = "block";

    if (list !== undefined) {
        for (let i = 1; i <= list.length; i++) {
            let char = list[i - 1];

            let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
            charBtn.innerText = char.name;
            charBtn.className = "char-name";
            charBtn.classList.add("char");
            charBtn.onclick = () => {
                enableButtons(false);
                Net.instance.gameCall("selectCharacter", {id: char.id})
                    .then((d) => {
                        Client.instance.character = d;
                        hideCharactersList();
                        Game.start();
                    });
            };

            let delBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("del-char" + i));
            delBtn.onclick = () => {
                enableButtons(false);
                Net.instance.gameCall("deleteCharacter", {id: char.id})
                    .then((d) => {
                        showCharactersList(d);
                        enableButtons(true);
                    });
            };
        }
        for (let i = list.length + 1; i <= 5; i++) {
            let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
            charBtn.innerText = "EMPTY SLOT";
            charBtn.className = "char-empty";
            charBtn.classList.add("char");
            charBtn.onclick = () => {
                hideCharactersList();
                showCharacterCreate();
            };
        }
    }

    let logoutBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("logout-char"));
    logoutBtn.onclick = () => {
        enableButtons(false);

        localStorage.removeItem("login");
        localStorage.removeItem("password");

        hideCharactersList();
        Net.instance.disconnect();
        showLoginPage();
    };
}

function enableButtons(val: boolean) {
    for (let i = 1; i <= 5; i++) {
        let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
        charBtn.disabled = !val;
        let delBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("del-char" + i));
        delBtn.disabled = !val;
    }
}

/**
 * спрятать список персонажей
 */
export function hideCharactersList() {
    for (let i = 1; i <= 5; i++) {
        let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
        charBtn.disabled = false;
        let delBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("del-char" + i));
        delBtn.disabled = false;
    }
    document.getElementById("characters-page").style.display = "none";
    document.getElementById("characters-list").style.display = "none";
}

/**
 * показать форму создания персонажа
 */
export function showCharacterCreate() {
    document.getElementById("characters-page").style.display = "block";
    document.getElementById("character-create").style.display = "block";

    let nameInput: HTMLInputElement = (<HTMLInputElement>document.getElementById("char-create-name"));

    // отмена
    let cancelBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char-create-cancel"));
    cancelBtn.onclick = () => {
        nameInput.value = "";
        hideCharacterCreate();
        showCharactersList();
    };
    // кнопка создания персонажа
    let confirmBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char-create"));
    confirmBtn.onclick = () => {
        let charName = nameInput.value;
        if (charName) {
            console.log(charName);
            Net.instance.gameCall("createCharacter", {name: charName})
                .then((d) => {
                    nameInput.value = "";
                    hideCharacterCreate();
                    showCharactersList(d);
                });
        }
    };
}

/**
 * спрятать диалог создания персонажа
 */
export function hideCharacterCreate() {
    document.getElementById("characters-page").style.display = "none";
    document.getElementById("character-create").style.display = "none";
}