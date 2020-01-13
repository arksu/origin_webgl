import Net from "./net/Net";

export function showCharacters(list: Array<any>) {
    document.getElementById("characters-page").style.display = "block";
    for (let i = 1; i <= list.length; i++) {
        let char = list[i - 1];

        let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
        charBtn.innerText = char.name;
        charBtn.onclick = () => {
            enableButtons(false);
            Net.instance.gameCall("selectCharacter", {id: char.id})
                .then(() => {
                    // TODO
                });
        };
        charBtn.className = "char-name";

        let delBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("del-char" + i));
        delBtn.onclick = () => {
            enableButtons(false);
            Net.instance.gameCall("deleteCharacter", {id: char.id})
                .then((d) => {
                    showCharacters(d);
                    enableButtons(true);
                });
        };
    }
    for (let i = list.length + 1; i <= 5; i++) {
        let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
        charBtn.innerText = "EMPTY SLOT";
        charBtn.onclick = () => {
            enableButtons(false);
            // Net.instance.gameCall("createCharacter", {slot: i});
            // TODO
        };
        charBtn.className = "char-empty";
    }
}

function enableButtons(val: boolean) {
    for (let i = 1; i <= 5; i++) {
        let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
        charBtn.disabled = !val;
        let delBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("del-char" + i));
        delBtn.disabled = !val;
    }
}

export function hideCharacters() {
    for (let i = 1; i <= 5; i++) {
        let charBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("char" + i));
        charBtn.innerText = ".";
        charBtn.onclick = undefined;
        charBtn.disabled = false;
        let delBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("del-char" + i));
        delBtn.disabled = false;
    }
    document.getElementById("characters-page").style.display = "none";
}