function GameCard({ game, onSelect }) {
  return (
    <article className="game-card" onClick={() => onSelect(game)}>
      <div className="game-card-body">
        <div>
          <h3>{game.name}</h3>
          <p>{game.region} • {game.country}</p>
          <p className="game-type">{game.type} • {game.difficulty}</p>
        </div>
        <button className="game-button">Voir</button>
      </div>
    </article>
  );
}

export default GameCard;
