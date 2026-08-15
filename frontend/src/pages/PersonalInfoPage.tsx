import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { api } from "../services/api";

type CurrentUser = {
  userId: number;
  email: string;
  role: "AGENT" | "RESPONSABLE" | "ADMIN";
  token: string;
};

type Profile = {
  userId: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string | null;
};

type Props = {
  user: CurrentUser;
  onUserUpdated: (user: CurrentUser) => void;
};

type Country = {
  code: string;
  flag: string;
  name: string;
  minDigits: number;
  maxDigits: number;
  pattern: RegExp;
};

const genericCountry = (code: string, flag: string, name: string): Country => ({
  code,
  flag,
  name,
  minDigits: 6,
  maxDigits: 14,
  pattern: /^\d{6,14}$/,
});

const countries: Country[] = [
  { code: "+216", flag: "🇹🇳", name: "Tunisie", minDigits: 8, maxDigits: 8, pattern: /^[24579]\d{7}$/ },
  { code: "+33", flag: "🇫🇷", name: "France", minDigits: 9, maxDigits: 9, pattern: /^[67]\d{8}$/ },
  { code: "+213", flag: "🇩🇿", name: "Algérie", minDigits: 9, maxDigits: 9, pattern: /^[567]\d{8}$/ },
  { code: "+212", flag: "🇲🇦", name: "Maroc", minDigits: 9, maxDigits: 9, pattern: /^[67]\d{8}$/ },
  { code: "+32", flag: "🇧🇪", name: "Belgique", minDigits: 9, maxDigits: 9, pattern: /^\d{9}$/ },
  { code: "+1", flag: "🇨🇦", name: "Canada", minDigits: 10, maxDigits: 10, pattern: /^\d{10}$/ },
  { code: "+49", flag: "🇩🇪", name: "Allemagne", minDigits: 10, maxDigits: 11, pattern: /^\d{10,11}$/ },
  { code: "+39", flag: "🇮🇹", name: "Italie", minDigits: 9, maxDigits: 10, pattern: /^\d{9,10}$/ },
  ...[
    ["+93", "🇦🇫", "Afghanistan"], ["+355", "🇦🇱", "Albanie"], ["+376", "🇦🇩", "Andorre"],
    ["+244", "🇦🇴", "Angola"], ["+1268", "🇦🇬", "Antigua-et-Barbuda"], ["+54", "🇦🇷", "Argentine"],
    ["+374", "🇦🇲", "Arménie"], ["+61", "🇦🇺", "Australie"], ["+43", "🇦🇹", "Autriche"],
    ["+994", "🇦🇿", "Azerbaïdjan"], ["+1242", "🇧🇸", "Bahamas"], ["+973", "🇧🇭", "Bahreïn"],
    ["+880", "🇧🇩", "Bangladesh"], ["+1246", "🇧🇧", "Barbade"], ["+375", "🇧🇾", "Biélorussie"],
    ["+501", "🇧🇿", "Belize"], ["+229", "🇧🇯", "Bénin"], ["+975", "🇧🇹", "Bhoutan"],
    ["+591", "🇧🇴", "Bolivie"], ["+387", "🇧🇦", "Bosnie-Herzégovine"], ["+267", "🇧🇼", "Botswana"],
    ["+55", "🇧🇷", "Brésil"], ["+673", "🇧🇳", "Brunei"], ["+359", "🇧🇬", "Bulgarie"],
    ["+226", "🇧🇫", "Burkina Faso"], ["+257", "🇧🇮", "Burundi"], ["+238", "🇨🇻", "Cap-Vert"],
    ["+855", "🇰🇭", "Cambodge"], ["+237", "🇨🇲", "Cameroun"], ["+236", "🇨🇫", "République centrafricaine"],
    ["+235", "🇹🇩", "Tchad"], ["+56", "🇨🇱", "Chili"], ["+86", "🇨🇳", "Chine"],
    ["+57", "🇨🇴", "Colombie"], ["+269", "🇰🇲", "Comores"], ["+242", "🇨🇬", "Congo"],
    ["+243", "🇨🇩", "République démocratique du Congo"], ["+506", "🇨🇷", "Costa Rica"], ["+385", "🇭🇷", "Croatie"],
    ["+53", "🇨🇺", "Cuba"], ["+357", "🇨🇾", "Chypre"], ["+420", "🇨🇿", "Tchéquie"],
    ["+45", "🇩🇰", "Danemark"], ["+253", "🇩🇯", "Djibouti"], ["+1767", "🇩🇲", "Dominique"],
    ["+1809", "🇩🇴", "République dominicaine"], ["+593", "🇪🇨", "Équateur"], ["+20", "🇪🇬", "Égypte"],
    ["+503", "🇸🇻", "Salvador"], ["+240", "🇬🇶", "Guinée équatoriale"], ["+291", "🇪🇷", "Érythrée"],
    ["+372", "🇪🇪", "Estonie"], ["+268", "🇸🇿", "Eswatini"], ["+251", "🇪🇹", "Éthiopie"],
    ["+679", "🇫🇯", "Fidji"], ["+358", "🇫🇮", "Finlande"], ["+241", "🇬🇦", "Gabon"],
    ["+220", "🇬🇲", "Gambie"], ["+995", "🇬🇪", "Géorgie"], ["+233", "🇬🇭", "Ghana"],
    ["+30", "🇬🇷", "Grèce"], ["+1473", "🇬🇩", "Grenade"], ["+502", "🇬🇹", "Guatemala"],
    ["+224", "🇬🇳", "Guinée"], ["+245", "🇬🇼", "Guinée-Bissau"], ["+592", "🇬🇾", "Guyana"],
    ["+509", "🇭🇹", "Haïti"], ["+504", "🇭🇳", "Honduras"], ["+36", "🇭🇺", "Hongrie"],
    ["+354", "🇮🇸", "Islande"], ["+91", "🇮🇳", "Inde"], ["+62", "🇮🇩", "Indonésie"],
    ["+98", "🇮🇷", "Iran"], ["+964", "🇮🇶", "Irak"], ["+353", "🇮🇪", "Irlande"],
    ["+972", "🇮🇱", "Israël"], ["+1876", "🇯🇲", "Jamaïque"], ["+81", "🇯🇵", "Japon"],
    ["+962", "🇯🇴", "Jordanie"], ["+7", "🇰🇿", "Kazakhstan"], ["+254", "🇰🇪", "Kenya"],
    ["+686", "🇰🇮", "Kiribati"], ["+965", "🇰🇼", "Koweït"], ["+996", "🇰🇬", "Kirghizistan"],
    ["+856", "🇱🇦", "Laos"], ["+371", "🇱🇻", "Lettonie"], ["+961", "🇱🇧", "Liban"],
    ["+266", "🇱🇸", "Lesotho"], ["+231", "🇱🇷", "Liberia"], ["+218", "🇱🇾", "Libye"],
    ["+423", "🇱🇮", "Liechtenstein"], ["+370", "🇱🇹", "Lituanie"], ["+352", "🇱🇺", "Luxembourg"],
    ["+261", "🇲🇬", "Madagascar"], ["+265", "🇲🇼", "Malawi"], ["+60", "🇲🇾", "Malaisie"],
    ["+960", "🇲🇻", "Maldives"], ["+223", "🇲🇱", "Mali"], ["+356", "🇲🇹", "Malte"],
    ["+692", "🇲🇭", "Îles Marshall"], ["+222", "🇲🇷", "Mauritanie"], ["+230", "🇲🇺", "Maurice"],
    ["+52", "🇲🇽", "Mexique"], ["+691", "🇫🇲", "Micronésie"], ["+373", "🇲🇩", "Moldavie"],
    ["+377", "🇲🇨", "Monaco"], ["+976", "🇲🇳", "Mongolie"], ["+382", "🇲🇪", "Monténégro"],
    ["+258", "🇲🇿", "Mozambique"], ["+95", "🇲🇲", "Myanmar"], ["+264", "🇳🇦", "Namibie"],
    ["+674", "🇳🇷", "Nauru"], ["+977", "🇳🇵", "Népal"], ["+31", "🇳🇱", "Pays-Bas"],
    ["+64", "🇳🇿", "Nouvelle-Zélande"], ["+505", "🇳🇮", "Nicaragua"], ["+227", "🇳🇪", "Niger"],
    ["+234", "🇳🇬", "Nigeria"], ["+850", "🇰🇵", "Corée du Nord"], ["+389", "🇲🇰", "Macédoine du Nord"],
    ["+47", "🇳🇴", "Norvège"], ["+968", "🇴🇲", "Oman"], ["+92", "🇵🇰", "Pakistan"],
    ["+680", "🇵🇼", "Palaos"], ["+970", "🇵🇸", "Palestine"], ["+507", "🇵🇦", "Panama"],
    ["+675", "🇵🇬", "Papouasie-Nouvelle-Guinée"], ["+595", "🇵🇾", "Paraguay"], ["+51", "🇵🇪", "Pérou"],
    ["+63", "🇵🇭", "Philippines"], ["+48", "🇵🇱", "Pologne"], ["+351", "🇵🇹", "Portugal"],
    ["+974", "🇶🇦", "Qatar"], ["+40", "🇷🇴", "Roumanie"], ["+7", "🇷🇺", "Russie"],
    ["+250", "🇷🇼", "Rwanda"], ["+1869", "🇰🇳", "Saint-Christophe-et-Niévès"], ["+1758", "🇱🇨", "Sainte-Lucie"],
    ["+1784", "🇻🇨", "Saint-Vincent-et-les-Grenadines"], ["+685", "🇼🇸", "Samoa"], ["+378", "🇸🇲", "Saint-Marin"],
    ["+239", "🇸🇹", "Sao Tomé-et-Principe"], ["+966", "🇸🇦", "Arabie saoudite"], ["+221", "🇸🇳", "Sénégal"],
    ["+381", "🇷🇸", "Serbie"], ["+248", "🇸🇨", "Seychelles"], ["+232", "🇸🇱", "Sierra Leone"],
    ["+65", "🇸🇬", "Singapour"], ["+421", "🇸🇰", "Slovaquie"], ["+386", "🇸🇮", "Slovénie"],
    ["+677", "🇸🇧", "Îles Salomon"], ["+252", "🇸🇴", "Somalie"], ["+27", "🇿🇦", "Afrique du Sud"],
    ["+82", "🇰🇷", "Corée du Sud"], ["+211", "🇸🇸", "Soudan du Sud"], ["+34", "🇪🇸", "Espagne"],
    ["+94", "🇱🇰", "Sri Lanka"], ["+249", "🇸🇩", "Soudan"], ["+597", "🇸🇷", "Suriname"],
    ["+46", "🇸🇪", "Suède"], ["+41", "🇨🇭", "Suisse"], ["+963", "🇸🇾", "Syrie"],
    ["+886", "🇹🇼", "Taïwan"], ["+992", "🇹🇯", "Tadjikistan"], ["+255", "🇹🇿", "Tanzanie"],
    ["+66", "🇹🇭", "Thaïlande"], ["+670", "🇹🇱", "Timor oriental"], ["+228", "🇹🇬", "Togo"],
    ["+676", "🇹🇴", "Tonga"], ["+1868", "🇹🇹", "Trinité-et-Tobago"], ["+90", "🇹🇷", "Turquie"],
    ["+993", "🇹🇲", "Turkménistan"], ["+688", "🇹🇻", "Tuvalu"], ["+256", "🇺🇬", "Ouganda"],
    ["+380", "🇺🇦", "Ukraine"], ["+971", "🇦🇪", "Émirats arabes unis"], ["+44", "🇬🇧", "Royaume-Uni"],
    ["+1", "🇺🇸", "États-Unis"], ["+598", "🇺🇾", "Uruguay"], ["+998", "🇺🇿", "Ouzbékistan"],
    ["+678", "🇻🇺", "Vanuatu"], ["+58", "🇻🇪", "Venezuela"], ["+84", "🇻🇳", "Viêt Nam"],
    ["+967", "🇾🇪", "Yémen"], ["+260", "🇿🇲", "Zambie"], ["+263", "🇿🇼", "Zimbabwe"],
  ].map(([code, flag, name]) => genericCountry(code, flag, name)),
];

const getErrorMessage = (error: unknown) => {
  const response = (error as { response?: { data?: { detail?: string; message?: string } } })
    .response;
  return response?.data?.detail || response?.data?.message || "Une erreur est survenue.";
};

function PersonalInfoPage({ user, onUserUpdated }: Props) {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [form, setForm] = useState({ nom: "", prenom: "", email: "", telephone: "" });
  const [countryCode, setCountryCode] = useState("+216");
  const [phoneError, setPhoneError] = useState("");
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadProfile = (nextProfile: Profile) => {
    const country = countries.find((item) => nextProfile.telephone?.startsWith(item.code));
    const selectedCountryCode = country?.code || "+216";
    const phone = nextProfile.telephone
      ? nextProfile.telephone.replace(selectedCountryCode, "").replace(/[^\d]/g, "")
      : "";

    setProfile(nextProfile);
    setCountryCode(selectedCountryCode);
    setForm({
      nom: nextProfile.nom || "",
      prenom: nextProfile.prenom || "",
      email: nextProfile.email || "",
      telephone: phone,
    });
  };

  useEffect(() => {
    api.get<Profile>("/profile/me")
      .then((response) => {
        loadProfile(response.data);
      })
      .catch((requestError) => setError(getErrorMessage(requestError)))
      .finally(() => setLoading(false));
  }, []);

  const handleEdit = () => {
    if (!profile) return;
    setMessage("");
    setError("");
    setPhoneError("");
    setForm({
      nom: profile.nom || "",
      prenom: profile.prenom || "",
      email: profile.email || "",
      telephone: profile.telephone?.replace(countryCode, "").replace(/[^\d]/g, "") || "",
    });
    setEditing(true);
  };

  const handleCancel = () => {
    setMessage("");
    setError("");
    setPhoneError("");
    setEditing(false);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");
    setPhoneError("");

    const selectedCountry = countries.find((country) => country.code === countryCode);
    const phone = form.telephone.replace(/\D/g, "");
    if (phone && selectedCountry && !selectedCountry.pattern.test(phone)) {
      const digitLabel = selectedCountry.minDigits === selectedCountry.maxDigits
        ? `${selectedCountry.minDigits}`
        : `${selectedCountry.minDigits} à ${selectedCountry.maxDigits}`;
      setPhoneError(
        `Le numéro doit contenir ${digitLabel} chiffres valides pour ${selectedCountry.name}.`
      );
      setSaving(false);
      return;
    }

    try {
      const response = await api.put<Profile & { token: string }>("/profile/me", {
        ...form,
        telephone: phone ? `${countryCode} ${phone}` : "",
      });
      const updatedProfile = response.data;
      loadProfile(updatedProfile);
      onUserUpdated({
        ...user,
        email: updatedProfile.email,
        token: updatedProfile.token,
      });
      setEditing(false);
      setMessage("Vos informations personnelles ont été mises à jour avec succès.");
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="container settings-page"><section className="card"><p>Chargement...</p></section></div>;
  }

  return (
    <div className="container settings-page">
      <section className="card personal-info-card">
        <div className="settings-section-heading">
          <div>
            <h1>Informations personnelles</h1>
            <p>Consultez et gérez les informations associées à votre compte.</p>
          </div>
        </div>

        {message && <div className="message success">{message}</div>}
        {error && <div className="message error">{error}</div>}

        {editing ? (
          <form className="personal-info-form" onSubmit={handleSubmit}>
            <label>
              Nom
              <input
                value={form.nom}
                onChange={(event) => setForm({ ...form, nom: event.target.value })}
                required
              />
            </label>
            <label>
              Prénom
              <input
                value={form.prenom}
                onChange={(event) => setForm({ ...form, prenom: event.target.value })}
                required
              />
            </label>
            <label>
              Adresse e-mail
              <input
                type="email"
                value={form.email}
                onChange={(event) => setForm({ ...form, email: event.target.value })}
                required
              />
              </label>
              <div className="phone-field-group">
                <label className="phone-label">
                  Téléphone <span className="optional-field">(facultatif)</span>
                </label>
                <div className="phone-fields">
                  <label>
                    <span className="visually-hidden">Indicatif téléphonique</span>
                    <select value={countryCode} onChange={(event) => {
                      setCountryCode(event.target.value);
                      setPhoneError("");
                    }}>
                      {countries.map((country) => (
                        <option key={country.code} value={country.code}>
                          {country.flag} {country.code}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    <span className="visually-hidden">Numéro de téléphone</span>
                    <input
                      type="tel"
                      value={form.telephone}
                      onChange={(event) => {
                        setForm({ ...form, telephone: event.target.value });
                        setPhoneError("");
                      }}
                      placeholder="12 345 678"
                    />
                  </label>
                </div>
                {phoneError && <span className="field-error">{phoneError}</span>}
                <span className="form-help">Sélectionnez le pays puis saisissez le numéro sans l’indicatif.</span>
              </div>
            <div className="personal-info-actions">
              <button type="submit" className="personal-info-save" disabled={saving}>
                {saving ? "Enregistrement..." : "Valider"}
              </button>
              <button type="button" className="personal-info-cancel" onClick={handleCancel} disabled={saving}>
                Annuler
              </button>
            </div>
          </form>
        ) : (
          <div className="personal-info-details">
            <div><span>Nom</span><strong>{profile?.nom || "—"}</strong></div>
            <div><span>Prénom</span><strong>{profile?.prenom || "—"}</strong></div>
            <div><span>Adresse e-mail</span><strong>{profile?.email || "—"}</strong></div>
            <div><span>Téléphone</span><strong>{profile?.telephone || "Non renseigné"}</strong></div>
            <div className="personal-info-actions">
              <button type="button" className="personal-info-edit" onClick={handleEdit}>
                Modifier
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}

export default PersonalInfoPage;
