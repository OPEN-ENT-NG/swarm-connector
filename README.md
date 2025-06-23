# swarm-connector

# À propos de l'application Swarm-connector
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright CGI
* Développeur : CGI
* Financeurs : CRNA
* Description : Connecteur permettant d'alimenter la Ferme numérique

# Présentation du module
L'application **Swarm-connector** est un connectreur permettant de fournir de façon efficace les informations nécessaires au bon fonctionnement de l'application **Ferme numérique**
Les infos en question sont du type :
```
{
    firstName: string,
    lastName: string,
    mail: string,
    structures: {
        id: string,
        name: string
    },
    classes: {
        id: string,
        name: string
    },
    groups: {
        id: string,
        name: string
    },
}
```

## Configuration
Pas de configuration ou de variable d'environnement spécifique
