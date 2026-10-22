import collections

def rows(page_html):
    out = []
    for ri, row in re.findall(r'<tr[^>]*data-ri="(\d+)"[^>]*>(.*?)</tr>', page_html, re.S):
        text = re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", row)).strip()
        out.append((int(ri), text))
    return out


def new_row(before_html, after_html):
    b = collections.Counter(t for _, t in rows(before_html))
    for ri, t in rows(after_html):
        if b[t] > 0:
            b[t] -= 1
        else:
            return ri, t
    return None, None


def select_options(page_html, name):
    m = re.search(r'<select[^>]*name="' + re.escape(name) + r'"[^>]*>(.*?)</select>', page_html, re.S)
    return [html.unescape(v) for v in re.findall(r'<option[^>]*value="([^"]+)"', m.group(1))] if m else []


def pick(page_html, name, word):
    m = re.search(r'<select[^>]*name="' + re.escape(name) + r'"[^>]*>(.*?)</select>', page_html, re.S)
    for v, label in re.findall(r'<option[^>]*value="([^"]+)"[^>]*>([^<]*)', m.group(1)):
        if word in label:
            return html.unescape(v)
    return None


def act(pg, form, btn, **kw):
    st, body = pg.ajax(form, btn, **kw)
    return body, ajax_error(body)


# ---------------------------------------------------------------- Tableau de bord
pg = Page("/index.xhtml")
record("tableau de bord: page + donnees", pg.status == 200 and "Impossible de charger" not in pg.html
       and "Rotations du jour" in pg.html)

# ---------------------------------------------------------------- Cars
def edit_vehicle(pg):
    ri = [r for r, t in rows(pg.html) if "TEST 9999 CI 01" in t][0]
    edit = dict(pg.row_buttons("Modifier"))[ri]
    pg.ajax("listForm", edit, execute="@this", render="dialogForm")
    save = pg.button_id("Enregistrer", "dialogForm")
    body, errs = act(pg, "dialogForm", save, values={
        "plate": "TEST 9999 CI 01", "brand": "MarqueModifiee", "model": "M1",
        "cap_input": "30", "cap_hinput": "30", "st_input": "ENTRETIEN"},
        render="dlgMessages listForm:table growl")
    p2 = Page("/vehicles.xhtml")
    record("cars: modification", "MarqueModifiee" in p2.html and "En entretien" in p2.html and not errs,
           "; ".join(errs))
    return p2

crud_page("cars", "/vehicles.xhtml", {
    "plate": "TEST 9999 CI 01", "brand": "MarqueTest", "model": "M1",
    "cap_input": "30", "cap_hinput": "30", "st_input": "DISPONIBLE"},
    "TEST 9999 CI 01", prepare_label="Nouveau car",
    extra=edit_vehicle)

# ---------------------------------------------------------------- Usagers
def suspend_passenger(pg):
    ri = [r for r, t in rows(pg.html) if "Testeur E2E" in t][0]
    btn = dict(pg.row_buttons("Suspendre"))[ri]
    pg.ajax("listForm", btn, execute="@this", render="table growl")
    p2 = Page("/passengers.xhtml")
    record("usagers: suspension", any("Testeur E2E" in t and "Suspendu" in t for _, t in rows(p2.html)))
    return p2

crud_page("usagers", "/passengers.xhtml", {
    "name": "Testeur E2E", "phone": "0700000000", "st_input": "ACTIVE"},
    "Testeur E2E", prepare_label="Nouvel usager", extra=suspend_passenger)

# ---------------------------------------------------------------- Lignes
def toggle_route(pg):
    ri = [r for r, t in rows(pg.html) if "T99" in t][0]
    btn = dict(pg.row_buttons("Suspendre la ligne"))[ri]
    pg.ajax("listForm", btn, execute="@this", render="table growl")
    p2 = Page("/routes.xhtml")
    record("lignes: suspension", any("T99" in t and "Suspendue" in t for _, t in rows(p2.html)))
    return p2

crud_page("lignes", "/routes.xhtml", {
    "code": "T99", "name": "Ligne test E2E", "org": "Abidjan", "dst": "Bouake",
    "km_input": "12", "km_hinput": "12", "st_input": "ACTIVE"},
    "T99", prepare_label="Nouvelle ligne", extra=toggle_route)

# ---------------------------------------------------------------- Chauffeurs
crud_page("chauffeurs", "/drivers.xhtml", {
    "name": "Chauffeur E2E", "phone": "0711111111", "lic": "PERM-E2E", "cat": "D",
    "exp_input": "31/12/2030", "rem_input": "FIXE", "st_input": "DISPONIBLE",
    "sal_input": "150000", "sal_hinput": "150000"},
    "Chauffeur E2E", prepare_label="Nouveau chauffeur")

# ---------------------------------------------------------------- Documents d'un car
pg = Page("/documents.xhtml")
cars = select_options(pg.html, "listForm:veh_input")
record("documents: liste des cars", len(cars) > 0)
if cars:
    body, errs = act(pg, "listForm", "listForm:veh", values={"listForm:veh_input": cars[0]},
                     event="change", execute="listForm:veh", render="listForm:table listForm:addBtn growl")
    record("documents: selection d'un car", not errs, "; ".join(errs))
    pg.ajax("actionsForm", pg.button_id("Nouveau document", "actionsForm"), execute="@this", render="dialogForm")
    body, errs = act(pg, "dialogForm", pg.button_id("Enregistrer", "dialogForm"), values={
        "type_input": "ASSURANCE", "ref": "E2E-REF-1", "iss": "AssureurE2E", "exp_input": "31/12/2031"},
        render="dlgMessages listForm:table growl")
    p2 = Page("/documents.xhtml")
    # la selection vit dans la vue ; on controle via la reponse ajax du formulaire de liste
    st, tbl = pg.ajax("listForm", "listForm:veh", {"listForm:veh_input": cars[0]}, event="change",
                      execute="listForm:veh", render="listForm:table")
    record("documents: creation", "E2E-REF-1" in tbl and not errs, "; ".join(errs))
    if "E2E-REF-1" in tbl:
        ri = [r for r, t in rows(tbl.replace("<![CDATA[", "").replace("]]>", "")) if "E2E-REF-1" in t]
        dels = dict(pg.row_buttons("Supprimer"))
        # le tableau a ete re-rendu : on retrouve les boutons dans la reponse ajax
        dd = dict((int(m.group(2)), m.group(1)) for m in re.finditer(
            r'<button[^>]*id="(listForm:table:(\d+):[^"]+)"[^>]*title="Supprimer"', html.unescape(tbl)))
        idx = ri[0] if ri else None
        if idx in dd:
            pg.ajax("listForm", dd[idx], execute="@this", render="listForm:table growl")
            st, tbl2 = pg.ajax("listForm", "listForm:veh", {"listForm:veh_input": cars[0]}, event="change",
                               execute="listForm:veh", render="listForm:table")
            record("documents: suppression", "E2E-REF-1" not in tbl2)
        else:
            record("documents: suppression (bouton introuvable)", False)

# ---------------------------------------------------------------- Entretiens d'un car
pg = Page("/maintenances.xhtml")
cars = select_options(pg.html, "listForm:veh_input")
if cars:
    act(pg, "listForm", "listForm:veh", values={"listForm:veh_input": cars[0]}, event="change",
        execute="listForm:veh", render="listForm:table listForm:addBtn growl")
    pg.ajax("actionsForm", pg.button_id("Nouvel entretien", "actionsForm"), execute="@this", render="dialogForm")
    body, errs = act(pg, "dialogForm", pg.button_id("Enregistrer", "dialogForm"), values={
        "type_input": "VIDANGE", "st_input": "PLANIFIE", "sch_input": "15/11/2030",
        "gar": "GarageE2E", "cost_input": "45000", "cost_hinput": "45000"},
        render="dlgMessages listForm:table growl")
    st, tbl = pg.ajax("listForm", "listForm:veh", {"listForm:veh_input": cars[0]}, event="change",
                      execute="listForm:veh", render="listForm:table")
    record("entretiens: creation", "GarageE2E" in tbl and not errs, "; ".join(errs))
    dd = dict((int(m.group(2)), m.group(1)) for m in re.finditer(
        r'<button[^>]*id="(listForm:table:(\d+):[^"]+)"[^>]*title="Supprimer"', html.unescape(tbl)))
    ri = [r for r, t in rows(html.unescape(tbl)) if "GarageE2E" in t]
    if ri and ri[0] in dd:
        pg.ajax("listForm", dd[ri[0]], execute="@this", render="listForm:table growl")
        st, tbl2 = pg.ajax("listForm", "listForm:veh", {"listForm:veh_input": cars[0]}, event="change",
                           execute="listForm:veh", render="listForm:table")
        record("entretiens: suppression", "GarageE2E" not in tbl2)
    else:
        record("entretiens: suppression (bouton introuvable)", False)

# ---------------------------------------------------------------- Horaires d'une ligne
pg = Page("/schedules.xhtml")
routes = select_options(pg.html, "listForm:rte_input")
if routes:
    act(pg, "listForm", "listForm:rte", values={"listForm:rte_input": routes[0]}, event="change",
        execute="listForm:rte", render="listForm:table listForm:addBtn growl")
    pg.ajax("actionsForm", pg.button_id("Nouvel horaire", "actionsForm"), execute="@this", render="dialogForm")
    body, errs = act(pg, "dialogForm", pg.button_id("Enregistrer", "dialogForm"), values={
        "day_input": "DIMANCHE", "time_input": "23:45"}, render="dlgMessages listForm:table growl")
    st, tbl = pg.ajax("listForm", "listForm:rte", {"listForm:rte_input": routes[0]}, event="change",
                      execute="listForm:rte", render="listForm:table")
    record("horaires: creation", "23:45" in tbl and not errs, "; ".join(errs))
    dd = dict((int(m.group(2)), m.group(1)) for m in re.finditer(
        r'<button[^>]*id="(listForm:table:(\d+):[^"]+)"[^>]*title="Supprimer"', html.unescape(tbl)))
    ri = [r for r, t in rows(html.unescape(tbl)) if "23:45" in t]
    if ri and ri[0] in dd:
        pg.ajax("listForm", dd[ri[0]], execute="@this", render="listForm:table growl")
        st, tbl2 = pg.ajax("listForm", "listForm:rte", {"listForm:rte_input": routes[0]}, event="change",
                           execute="listForm:rte", render="listForm:table")
        record("horaires: suppression", "23:45" not in tbl2)
    else:
        record("horaires: suppression (bouton introuvable)", False)

# ---------------------------------------------------------------- Locations
before = Page("/rentals.xhtml")
pax = select_options(before.html, "dialogForm:pax_input")
veh = select_options(before.html, "dialogForm:veh_input")
pg = Page("/rentals.xhtml")
pg.ajax("actionsForm", pg.button_id("Nouvelle location", "actionsForm"), execute="@this", render="dialogForm")
body, errs = act(pg, "dialogForm", pg.button_id("Enregistrer", "dialogForm"), values={
    "pax_input": pax[0], "veh_input": pick(before.html, "dialogForm:veh_input", "Disponible") or veh[0], "from_input": "01/12/2030", "to_input": "05/12/2030",
    "total_input": "73123", "total_hinput": "73123"}, render="dlgMessages listForm:table growl")
after = Page("/rentals.xhtml")
ri, txt = new_row(before.html, after.html)
record("locations: creation", ri is not None and not errs, "; ".join(errs))
if ri is not None:
    for title, label, expect in (("Démarrer", "démarrage", "En cours"), ("Terminer", "fin", "Terminée")):
        p = Page("/rentals.xhtml")
        r_i = [r for r, t in rows(p.html) if t == txt or t.replace("RESERVEE", "").strip() in t][0] if False else None
        idx, _ = new_row(before.html, p.html)
        btn = dict(p.row_buttons(title)).get(idx)
        if btn:
            p.ajax("listForm", btn, execute="@this", render="table growl")
            p2 = Page("/rentals.xhtml")
            record(f"locations: {label}", any(expect in t for r, t in rows(p2.html) if r == idx))
        else:
            record(f"locations: {label} (bouton introuvable)", False)
    p = Page("/rentals.xhtml")
    idx, _ = new_row(before.html, p.html)
    btn = dict(p.row_buttons("Supprimer")).get(idx)
    p.ajax("listForm", btn, execute="@this", render="table growl")
    p2 = Page("/rentals.xhtml")
    record("locations: suppression", len(rows(p2.html)) == len(rows(before.html)))

# ---------------------------------------------------------------- Abonnements
before = Page("/subscriptions.xhtml")
pax = select_options(before.html, "dialogForm:pax_input")
pg = Page("/subscriptions.xhtml")
pg.ajax("actionsForm", pg.button_id("Nouvel abonnement", "actionsForm"), execute="@this", render="dialogForm")
body, errs = act(pg, "dialogForm", pg.button_id("Enregistrer", "dialogForm"), values={
    "pax_input": pax[0], "plan_input": "MENSUEL", "amount_input": "15000", "amount_hinput": "15000",
    "method_input": "ESPECES"}, render="dlgMessages listForm:table growl")
after = Page("/subscriptions.xhtml")
idx, txt = new_row(before.html, after.html)
record("abonnements: creation", idx is not None and not errs, "; ".join(errs))
if idx is not None:
    btn = dict(after.row_buttons("Annuler l'abonnement")).get(idx)
    if btn:
        after.ajax("listForm", btn, execute="@this", render="table growl")
        p2 = Page("/subscriptions.xhtml")
        record("abonnements: annulation", any("Annulé" in t for r, t in rows(p2.html) if r == idx))
    else:
        record("abonnements: annulation (bouton introuvable)", False)
    # Un abonnement annule garde ses paiements (historique financier) : la suppression doit
    # etre refusee proprement (message clair), sans erreur serveur.
    p = Page("/subscriptions.xhtml")
    btn = dict(p.row_buttons("Supprimer")).get(idx)
    st, body = p.ajax("listForm", btn, execute="@this", render="table growl")
    errs = ajax_error(body)
    p2 = Page("/subscriptions.xhtml")
    record("abonnements: suppression refusee proprement (paiements)",
           any("paiements" in e for e in errs) and len(rows(p2.html)) == len(rows(before.html)) + 1,
           "; ".join(errs) or "aucun message")

# ---------------------------------------------------------------- Rotations
before = Page("/rotations.xhtml")
drv = select_options(before.html, "dialogForm:drv_input")
veh = select_options(before.html, "dialogForm:veh_input")
rte = select_options(before.html, "dialogForm:rte_input")
pg = Page("/rotations.xhtml")
pg.ajax("actionsForm", pg.button_id("Nouvelle rotation", "actionsForm"), execute="@this", render="dialogForm")
body, errs = act(pg, "dialogForm", pg.button_id("Enregistrer", "dialogForm"), values={
    "drv_input": pick(before.html, "dialogForm:drv_input", "Disponible") or drv[0], "veh_input": pick(before.html, "dialogForm:veh_input", "Disponible") or veh[0], "rte_input": rte[0], "start_input": "01/12/2030 08:00",
    "end_input": "01/12/2030 10:00", "notes": "rotation E2E"}, render="dlgMessages listForm:table growl")
after = Page("/rotations.xhtml")
idx, txt = new_row(before.html, after.html)
record("rotations: creation", idx is not None and not errs, "; ".join(errs))
if idx is not None:
    for title, label, expect in (("Démarrer", "démarrage", "En cours"), ("Terminer", "fin", "Terminée")):
        p = Page("/rotations.xhtml")
        i2, _ = new_row(before.html, p.html)
        btn = dict(p.row_buttons(title)).get(i2)
        if btn:
            p.ajax("listForm", btn, execute="@this", render="table growl")
            p2 = Page("/rotations.xhtml")
            record(f"rotations: {label}", any(expect in t for r, t in rows(p2.html) if r == i2))
        else:
            record(f"rotations: {label} (bouton introuvable)", False)
    p = Page("/rotations.xhtml")
    i2, _ = new_row(before.html, p.html)
    btn = dict(p.row_buttons("Supprimer")).get(i2)
    p.ajax("listForm", btn, execute="@this", render="table growl")
    p2 = Page("/rotations.xhtml")
    record("rotations: suppression", len(rows(p2.html)) == len(rows(before.html)))

# ---------------------------------------------------------------- Recherche et filtres
pg = Page("/vehicles.xhtml")
total = len(rows(pg.html))
term = rows(pg.html)[0][1].split()[0]          # premier mot du premier car (independant des donnees)
st, body = pg.ajax("listForm", "listForm:q", {"listForm:q": term}, event="keyup",
                   execute="listForm:q", render="listForm:table listForm:count")
n_found = len(rows(html.unescape(body)))
record(f"recherche: filtre les cars ({term})", 0 < n_found <= total and (n_found < total or total == 1),
       f"{n_found} resultats sur {total}")
st, body = pg.ajax("listForm", "listForm:q", {"listForm:q": "zzzzintrouvable"}, event="keyup",
                   execute="listForm:q", render="listForm:table listForm:count")
record("recherche: aucun resultat affiche l'etat vide", "Aucun car trouvé" in html.unescape(body))

# ---------------------------------------------------------------- Deconnexion
st, body, url = http(BASE + "/logout")
st2, body2, url2 = http(BASE + "/vehicles.xhtml")
record("deconnexion: session fermee (retour au login)", "openid-connect" in url2 or "Sign in" in body2
       or "kc-form-login" in body2 or "username" in body2)
