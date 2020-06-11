# ChatPronouns
A simple plugin to display a person's preferred pronouns in the chat

## Setting Pronouns
By default, the plugin can display male, female and non-binary pronouns. You can set pronouns by
using `/setpronouns (m/f/n)`. Staff members can set another person's pronouns using `/setpronouns (m/f/n) [name]`.

Staff members can also set customized pronouns on a per-user basis, using `/setcustompronouns (name) (tag) (description)`

## Permissions

The plugin has two permissions `chatpronouns.custom`, giving the user access to the `/setcustompermissions` command, and 
`chatpronouns.other` allowing the user to set another persons pronouns.

## PlaceholderAPI

The plugin supports PlaceholderAPI, with 3 valid placeholders:
```
%chatpronouns_tag% -> Displays the tag surrounded by white [] brackets
%chatpronouns_miniature_tag% -> Displays the tag without brackets
%chatpronouns_hover_pronouns% -> Shows the description of the pronouns
```