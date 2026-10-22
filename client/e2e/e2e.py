"""Test general de l'administration : se connecte via Keycloak puis rejoue les
requetes JSF/ajax des boutons (creation, changement de statut, suppression)."""
import html, http.cookiejar, os, re, sys, urllib.parse, urllib.request, urllib.error
from html.parser import HTMLParser

BASE = os.environ.get("ADMIN_URL", "http://localhost:8081/transit")
USER = os.environ.get("ADMIN_USER", "admin")
PASSWORD = os.environ.get("ADMIN_PASSWORD", "Transit2026!")
# Keycloak 26 pose ses cookies de session en Secure meme sur http://localhost (accepte par les
# navigateurs) : on l'autorise aussi ici.
jar = http.cookiejar.CookieJar(policy=http.cookiejar.DefaultCookiePolicy(secure_protocols=("https", "http", "wss", "ws")))
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
results = []


def http(url, data=None, headers=None):
    req = urllib.request.Request(url, data=data, headers=headers or {})
    try:
        with opener.open(req, timeout=30) as r:
            return r.status, r.read().decode("utf-8", "replace"), r.geturl()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace"), url


def login():
    st, body, url = http(BASE + "/index.xhtml")
    m = re.search(r'action="([^"]+)"', body)
    action = html.unescape(m.group(1))
    st, body, url = http(action, urllib.parse.urlencode(
        {"username": USER, "password": PASSWORD}).encode(),
        {"Content-Type": "application/x-www-form-urlencoded"})
    return st == 200 and "Transit Admin" in body


class FormParser(HTMLParser):
    def __init__(self, form_id):
        super().__init__()
        self.form_id, self.inside, self.fields = form_id, False, []
        self.select, self.select_val, self.textarea = None, None, None

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == "form":
            self.inside = a.get("id") == self.form_id
        if not self.inside:
            return
        if tag == "input" and a.get("name"):
            t = a.get("type", "text")
            if t in ("checkbox", "radio") and "checked" not in a:
                return
            if t in ("button", "submit", "image"):
                return
            self.fields.append([a["name"], a.get("value", "")])
        elif tag == "select" and a.get("name"):
            self.select, self.select_val = a["name"], ""
            self.fields.append([a["name"], ""])
        elif tag == "option" and self.select and "selected" in a:
            self.fields[-1][1] = a.get("value", "")
        elif tag == "textarea" and a.get("name"):
            self.fields.append([a["name"], ""])

    def handle_endtag(self, tag):
        if tag == "form":
            self.inside = False
        if tag == "select":
            self.select = None


class Page:
    def __init__(self, path):
        self.path = path
        st, self.html, _ = http(BASE + path)
        self.status = st
        m = re.search(r'name="jakarta\.faces\.ViewState"[^>]*value="([^"]*)"', self.html) or \
            re.search(r'value="([^"]*)"[^>]*name="jakarta\.faces\.ViewState"', self.html)
        self.view_state = html.unescape(m.group(1)) if m else ""

    def form(self, form_id):
        p = FormParser(form_id)
        p.feed(self.html)
        return p.fields

    def button_id(self, label, scope=None):
        """id du bouton portant ce libelle (ou ce title)."""
        pat = r'<button[^>]*id="([^"]+)"[^>]*>(?:(?!</button>).)*?' + re.escape(label)
        for m in re.finditer(pat, self.html, re.S):
            if scope is None or m.group(1).startswith(scope):
                return m.group(1)
        pat2 = r'<button[^>]*id="([^"]+)"[^>]*title="' + re.escape(label) + '"'
        for m in re.finditer(pat2, self.html):
            if scope is None or m.group(1).startswith(scope):
                return m.group(1)
        return None

    def row_buttons(self, title, table="listForm:table"):
        pat = r'<button[^>]*id="(' + re.escape(table) + r':(\d+):[^"]+)"[^>]*title="' + re.escape(title) + '"'
        return [(int(m.group(2)), m.group(1)) for m in re.finditer(pat, self.html)]

    def ajax(self, form_id, source, values=None, render="@all", event=None, execute="@all"):
        fields = self.form(form_id)
        data = {}
        for k, v in fields:
            data[k] = v
        for k, v in (values or {}).items():
            data[form_id + ":" + k if ":" not in k else k] = v
        data.update({
            form_id: form_id,
            "jakarta.faces.partial.ajax": "true",
            "jakarta.faces.source": source,
            "jakarta.faces.partial.execute": execute,
            "jakarta.faces.partial.render": render,
            "jakarta.faces.ViewState": self.view_state,
        })
        if event:
            data["jakarta.faces.behavior.event"] = event
            data["jakarta.faces.partial.event"] = event
        else:
            data[source] = source
        st, body, _ = http(BASE + self.path, urllib.parse.urlencode(data).encode(), {
            "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
            "Faces-Request": "partial/ajax", "X-Requested-With": "XMLHttpRequest"})
        return st, body


def record(name, ok, detail=""):
    results.append((name, ok, detail))
    print(("OK   " if ok else "FAIL ") + name + (" -- " + detail if detail and not ok else ""))


def ajax_error(body):
    """Message d'erreur eventuel renvoye par l'application dans la reponse ajax."""
    texts = re.findall(r'ui-growl-title[^>]*>([^<]+)|ui-messages-error-summary[^>]*>([^<]+)|'
                       r'ui-messages-error-detail[^>]*>([^<]+)', body)
    errs = [html.unescape(x) for t in texts for x in t if x]
    for m in re.finditer(r'summary:"([^"]*)",detail:"([^"]*)",severity:\'error\'', body):
        detail = m.group(2).replace("\\x22", '"')
        mm = re.search(r'"message":"([^"]*)"', detail)
        errs.append(m.group(1) + (": " + mm.group(1) if mm else ""))
    if "<error>" in body or "javax.faces" in body.lower() and "exception" in body.lower():
        errs.append("erreur serveur JSF")
    return errs


def crud_page(name, path, create_values, marker, listform="listForm", table="listForm:table",
              dialog_form="dialogForm", create_label="Enregistrer", prepare_label=None,
              extra=None):
    """Cree un enregistrement via le dialogue, verifie sa presence, puis le supprime."""
    pg = Page(path)
    record(f"{name}: page", pg.status == 200 and "Impossible de charger" not in pg.html, f"HTTP {pg.status}")
    # ouverture du dialogue (initialise le formulaire cote bean)
    if prepare_label:
        bid = pg.button_id(prepare_label, "actionsForm")
        st, _ = pg.ajax("actionsForm", bid, execute="@this", render=dialog_form)
    save = pg.button_id(create_label, dialog_form)
    if not save:
        return record(f"{name}: bouton Enregistrer trouve", False)
    st, body = pg.ajax(dialog_form, save, create_values, render="dlgMessages " + table + " growl")
    errs = ajax_error(body)
    pg2 = Page(path)
    present = marker in pg2.html
    record(f"{name}: creation", present and not errs, "; ".join(errs) or "absent de la liste")
    if not present:
        return pg2
    if extra:
        pg2 = extra(pg2) or pg2
    # suppression de la ligne creee
    idx = None
    rows = re.findall(r'<tr[^>]*data-ri="(\d+)"[^>]*>(.*?)</tr>', pg2.html, re.S)
    for ri, row in rows:
        if marker in row:
            idx = int(ri)
    dels = dict(pg2.row_buttons("Supprimer", table))
    if idx is None or idx not in dels:
        return record(f"{name}: suppression (bouton introuvable)", False)
    st, body = pg2.ajax(listform, dels[idx], execute="@this", render=table + " growl")
    pg3 = Page(path)
    record(f"{name}: suppression", marker not in pg3.html, "; ".join(ajax_error(body)))
    return pg3


if __name__ == "__main__":
    ok = login()
    record("connexion Keycloak", ok)
    if not ok:
        sys.exit(1)
    exec(open(sys.argv[1], encoding="utf-8").read())
    bad = [r for r in results if not r[1]]
    print(f"\n{len(results) - len(bad)}/{len(results)} verifications reussies")
    sys.exit(1 if bad else 0)
