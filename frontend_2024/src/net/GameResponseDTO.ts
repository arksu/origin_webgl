export default interface GameResponseDTO {
  id: number;

  /**
   * error if request is not success
   */
  e?: string;

  /**
   * channel
   */
  c?: string;

  /**
   * data
   */
  d: any;
}