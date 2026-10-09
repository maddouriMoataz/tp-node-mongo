const express = require("express");
const router = express.Router();
const Mesure = require("../models/mesure");

function handleError(res, err) {
  if (err.name === "ValidationError") {
    const erreurs = Object.values(err.errors).map(e => e.message);
    return res.status(400).json({ message: "Données invalides", erreurs });
  }
  console.error(err);
  return res.status(500).json({ message: "Erreur serveur" });
}

// CREATE -> 201
router.post("/", async (req, res) => {
  try {
    const mesure = new Mesure(req.body);
    await mesure.save();
    res.status(201).json(mesure);
  } catch (err) { handleError(res, err); }
});

// READ -> 200 : ?capteur=esp32-salle1&type=temperature&limit=50 (plus récentes d'abord)
router.get("/", async (req, res) => {
  try {
    const filtre = {};
    if (req.query.capteur) filtre.capteur = req.query.capteur;
    if (req.query.type) filtre.type = req.query.type;
    const limit = Math.min(parseInt(req.query.limit) || 50, 500);
    const mesures = await Mesure.find(filtre).sort({ date: -1 }).limit(limit);
    res.json(mesures);
  } catch (err) { handleError(res, err); }
});

module.exports = router;
